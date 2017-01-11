package co.phoenixlab.phoenixpac.tool;

import co.phoenixlab.phoenixpac.*;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.InflaterInputStream;

public class PacTool {

    public static void main(String[] args) {
        String cmd;
        boolean prompted = false;
        if (args.length == 0) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter a command: ");
            cmd = scanner.nextLine();
            prompted = true;
        } else {
            cmd = args[0];
        }
        String[] cmdArgs = new String[Math.max(0, args.length - 1)];
        if (!prompted) {
            System.arraycopy(args, 1, cmdArgs, 0, cmdArgs.length);
        }
        if ("pack".equalsIgnoreCase(cmd)) {
            pack(cmdArgs);
        } else if ("list".equalsIgnoreCase(cmd)) {
            list(cmdArgs);
        } else if ("unpack".equalsIgnoreCase(cmd)) {
            unpack(cmdArgs);
        }
    }

    private static void pack(String[] args) {

        //  TODO Add compression support

        //  TODO We may need to switch to a custom implementation
        //  for saving/building a PAC so we don't load all of the data into memory simultaneously

        Path mappings;
        if (args.length > 0) {
            mappings = Paths.get(args[0]);
        } else {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Specify the path to the build file: ");
            String path = scanner.nextLine();
            mappings = Paths.get(path);
        }
        if (!Files.isRegularFile(mappings)) {
            System.err.println(mappings.normalize().toAbsolutePath().toString() + " does not exist or is not a file");
            System.exit(1);
        }
        Map<TypePurposeUniqueId, Path> files = new HashMap<>(20);
        Path pacPath = Paths.get("out.pac");
        Path mappingsParent = mappings.getParent();
        try (BufferedReader reader = Files.newBufferedReader(mappings, StandardCharsets.UTF_8)) {
            Properties properties = new Properties();
            properties.load(reader);
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String key = (String) entry.getKey();
                String val = (String) entry.getValue();
                if ("output".equalsIgnoreCase(key)) {
                    pacPath = Paths.get(val);
                } else {
                    try {
                        TypePurposeUniqueId tpuid = TPU.valueOf(key);
                        Path path = mappingsParent.resolve(val).normalize().toAbsolutePath();
//                        Path path = Paths.get(val);
                        files.put(tpuid, path);
                        System.out.println("Indexed " + path.toString() + " as " + entry.getKey().toString());
                    } catch (Exception e) {
                        System.err.printf("Skipping invalid line %s=%s%n", key, val);
                    }
                }
            }
        } catch (FileNotFoundException ignored) {
        } catch (Exception e) {
            System.err.println("Unable to read devassets override config");
            e.printStackTrace();
            return;
        }
        pacPath = mappingsParent.resolve(pacPath).normalize().toAbsolutePath();
        int written = 0;
        try (PacFileWriter writer = new PacFileWriter(pacPath)) {
            HandlePacBuilder builder = HandlePacBuilder.newBuilder().buildHeader().
                    withLatestVersion().
                    finishHeader();
            //  Packing!
            for (Map.Entry<TypePurposeUniqueId, Path> entry : files.entrySet()) {
                Path file = entry.getValue();
                TypePurposeUniqueId key = entry.getKey();
                try {
                    System.out.println("Packing " + file.toString() + " as " + key.toString());
                    RawAssetHandle assetHandle = new RawAssetHandle(file);
                    int size = (int) Files.size(file);
                    builder = builder.newEntry().
                            withAssetHandle(assetHandle).
                            withSize(size).
                            withCompression(0).
                            withTPUID(key).
                            withComputedSha256Hash().
                            add();
                    // @formatter:off
                    builder = builder.buildMetadata().
                            editBlock(key).
                                addEntry("filename", mappingsParent.relativize(file).toString()).
                                add().
                            finish();
                    // @formatter:on
                    ++written;
                } catch (IOException e) {
                    System.err.println("Unable to add asset " + file.toString() + " @ " + key.toString());
                    e.printStackTrace();
                }
            }
            HandledPacFile<AssetHandle> res = builder.finish();
            writer.writeNew(res);
            System.out.printf("Packed %s assets to %s%n", written, pacPath.toString());
        } catch (Exception e) {
            System.err.println("Unable to add assets");
            e.printStackTrace();
        }
    }

    private static void list(String[] args) {
        Path file;
        if (args.length > 0) {
            file = Paths.get(args[0]);
        } else {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Specify the path to the PAC file: ");
            String path = scanner.nextLine();
            file = Paths.get(path);
        }
        if (!Files.isRegularFile(file)) {
            System.err.println(file.normalize().toAbsolutePath().toString() + " does not exist or is not a file");
            System.exit(1);
        }
        try (PacFileReader reader = new PacFileReader(file)) {
            PacFile pacFile = reader.read();
            LinkedHashMap<TypePurposeUniqueId, IndexEntry> entries = pacFile.getIndex().getEntries();
            System.out.printf("Files: %-8d%n", pacFile.getIndex().getNumIndexEntries());
            int count = 0;
            for (IndexEntry entry : entries.values()) {
                System.out.printf("%-3d pac://0x%04X/0x%04X/0x%08X d:%-10d m:%-10d c:%-2d h:%s%n",
                        count++,
                        entry.getTPUID().getTypeId(),
                        entry.getTPUID().getPurposeId(),
                        entry.getTPUID().getUniqueId(),
                        entry.getDiskSize(),
                        entry.getMemorySize(),
                        entry.getCompressionId(),
                        byteArrayToString(entry.getSha256Hash()));
            }
        } catch (Exception e) {
            System.err.println("Unable to list assets");
            e.printStackTrace();
        }
    }

    private static void unpack(String[] args) {
        Path file;
        if (args.length > 0) {
            file = Paths.get(args[0]);
        } else {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Specify the path to the PAC file: ");
            String path = scanner.nextLine();
            file = Paths.get(path);
        }
        if (!Files.isRegularFile(file)) {
            System.err.println(file.normalize().toAbsolutePath().toString() + " does not exist or is not a file");
            System.exit(1);
        }
        try (PacFileReader reader = new PacFileReader(file)) {
            PacFile pacFile = reader.read();
            LinkedHashMap<TypePurposeUniqueId, IndexEntry> entries = pacFile.getIndex().getEntries();
            Path parent = file.getParent();
            byte[] buffer = new byte[128 * 1024];
            int read;
            int completed;
            int total;
            for (IndexEntry entry : entries.values()) {
                TypePurposeUniqueId tpuid = entry.getTPUID();
                try {
                    String filename = String.format("0x%04X-0x%04X-0x%08X.bin", tpuid.getTypeId(), tpuid.getPurposeId(), tpuid.getUniqueId());
                    MetadataBlock block = pacFile.getMetadata().getMetadata().get(tpuid);
                    if (block != null) {
                        MetadataEntry metadataEntry = block.getEntries().get("filename");
                        if (metadataEntry != null) {
                            filename = metadataEntry.getVal();
                        }
                    }
                    Path target = parent.resolve(filename);
                    System.out.printf("Unpacking %s%n", target.toString());
                    AssetHandle handle = pacFile.getHandle(tpuid);
                    InputStream inputStream = handle.getRawStream();
                    total = entry.getMemorySize();
                    completed = 0;
                    Files.createDirectories(target.getParent());
                    int compId = handle.getCompressionId();
                    if (compId == 0) {
                        try (BufferedOutputStream outputStream =
                                 new BufferedOutputStream(Files.newOutputStream(target))) {
                            while ((read = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, read);
                                completed += read * 100;
                                System.out.printf("\r>>>> %d%%", (completed / total));
                            }
                            outputStream.flush();
                        }
                    } else if (compId == PacFile.COMPRESSION_DEFLATE) {
                        Files.deleteIfExists(target);
                        try (RandomAccessFile raf = new RandomAccessFile(target.toFile(), "rw")) {
                            ReadableByteChannel in = Channels.newChannel(new InflaterInputStream(inputStream));
                            FileChannel outCh = raf.getChannel();
                            long written = 0;
                            long writ;
                            while ((writ = outCh.transferFrom(in, written, 8192)) != 0) {
                                written += writ;
                                System.out.printf("\r>>>> %d%%", (100L * written) / total);
                            }
                        }
                    } else {
                        throw new UnsupportedOperationException("Unknown compression ID " + compId);
                    }

                    System.out.printf("%nUnpacked %s%n", target.toString());
                } catch (IOException e) {
                    System.err.println("Unable to export asset " + tpuid.toString());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("Unable to list assets");
            e.printStackTrace();
        }
    }


    private static String byteArrayToString(byte[] array) {
        StringBuilder builder = new StringBuilder(array.length * 2);
        for (byte b : array) {
            builder.append(String.format("%02X", b));
        }
        return builder.toString();
    }
}
