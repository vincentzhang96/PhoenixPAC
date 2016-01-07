PhoenixPAC Specification
=================

| Version   | 4.0 non-final |
|-----------|---------------|
| Author(s) | Vincent Zhang |
| Created   | Sep 23 2014   |
| Modified  | Mar 23 2015   |

Specification subject to change.

A Phoenix Packaged Asset Container (PPAC) file is an organized, managed collection of packed, optionally compressed
assets along with optional String-String metadata. Each asset file is assigned a unique identifier composed of an
integral type identifier, indicating the type of a file (equivalent to a file extension in a normal filesystem), an
integral purpose identifier, indicating what module or subsystem the asset belongs to, and an integral unique
identifier. Collectively this triplet is referred to as a TPU. Within a PPAC each TPU is unique, however between PPACs
this need not be true - in fact, a PPAC loader/asset mangement system can take advantage of this to allow for
overwriting of assets based on file load order.

Structure
----
PPAC files are big-endian.

File offsets can be stored either as 32-bit unsigned integers (max 4 GB offsets) or 64-bit signed integers (max
9 EB offsets). It is highly doubted that the full 9 exabyte reach will be necessary but it is useful to be able
to have PPACs larger than 4 GB in some cases (such as being used as a soundbank or storing multiple videos).
Which size is used depends on whether or not the `USE_LONG_OFFSETS` flag is set in the header (see appendix).

A PPAC file begins with a header identifying itself as a PPAC file, what version it is, and where the major parts
of the structure are located at. A PPAC file **must** contain an Index, even if it is empty. However, the Metadata and
Trash Index are optional. Within the PPAC file, there is no mandated ordering or positioning of asset data or the PPAC
structures besides that they must not overwrite the header (for obvious reasons).

The **Index** contains a list of **Index Entries** that identify an asset with a unique TPUID, followed by the size of
the asset on disk/in the file, where it is in the file, whether or not it is compressed and if yes how, and a hash to
ensure the validity of the asset package.

The **Metadata** section contains textual metadata about the PPAC and its assets. It consists of a list of **Metadata
Blocks** which contain lists of **Metadata Entries** for a specific TPUID. A **Metadata Entry** contains a pair of
UTF-8 strings containing the metadata key and value, of variable length between 0 and 255 bytes.

The **Trash Index** contains a list of "holes" - places in the PPAC that do not contain asset data or PPAC structures.
Each **Trash Index Entry** simply contains an offset to the trash and the length of the trash.
PPAC files that are modified can become fragmented because assets are deleted, change in size, or because of the
specific implementation of the PPAC manager used. This index allows for faster lookups into space usage which can be
used to grow existing assets without moving assets, to delete assets by removing the Index Entry and marking the space
as trash, to insert new assets without growing the file, or to shrink an asset by reducing the size listed in the
Index Entry and marking the space as trash. The Trash Index itself should be defragmented - immediately adjacent
entries should be merged into one entry. As a good practice, sections of the file that are marked as trash should be
filled with random/junk data or written over.

Asset files are stored in the PPAC with no extra data compressed or uncompressed as specified by the Index Entry, but
cannot exceed 4 GB in size on disk, or 2 GB if the `JAVA_ARRAY_COMPAT` flag is set.

##Format
----

`WIDE` sizes are 4 bytes, unless the `USE_LONG_OFFSETS` flag is set where `WIDE` is 8 bytes.

Fields that indicate the size of the structure exclude themselves from that value.

###Header
| Size       | Type     | Description |
|------------|----------|-------------|
| `4 BYTES`  | literal  | Magic number `0x50504143` 'PPAC' |
| `2 BYTES`  | integer  | Major version number (`4`) |
| `2 BYTES`  | integer  | Minor version number (`0`) |
| `16 BYTES` | n/a      | Reserved |
| `4 BYTES`  | bitflags | Flags (see appendix) |
| `WIDE`     | integer  | Index offset |
| `WIDE`     | integer  | Metadata section offset or `0` if no Metadata section |
| `WIDE`     | integer  | Trash Index offset or `0` if no Trash Index |


###Index
| Size       | Type     | Description |
|------------|----------|-------------|
| `4 BYTES ` | integer  | Number of Index Entries |
|            | set      | Set of Index Entries |
| `4 BYTES`  | int      | Guard bytes `0x494E4458` |

####Index Entry
| Size       | Type     | Description |
|------------|----------|-------------|
| `2 BYTES`  | integer  | Type ID |
| `2 BYTES`  | integer  | Purpose ID |
| `4 BYTES`  | integer  | Unique ID |
| `WIDE`     | integer  | Disk offset |
| `4 BYTES`  | integer  | Disk size - max 2GB|
| `4 BYTES`  | integer  | Memory (decompressed) size |
| `1 BYTE`   | integer  | Compression type (0 is no compression) |
| `3 BYTES`  | reserved | Reserved |
| `32 BYTES` | hash     | SHA-256 hash of the file |

###Metadata
| Size       | Type     | Description |
|------------|----------|-------------|
| `4 BYTES`  | integer  | Size of Metadata Section |
| `4 BYTES`  | integer  | Number of Metadata Blocks |
|            | set      | Set of Metadata Blocks |
| `4 BYTES`  | int      | Guard bytes `0x4D455441` |

####Metadata Block
| Size       | Type     | Description |
|------------|----------|-------------|
| `2 BYTES`  | integer  | Type ID |
| `2 BYTES`  | integer  | Purpose ID |
| `4 BYTES`  | integer  | Unique ID |
| `2 BYTES`  | integer  | Number of Metadata Entries |
| `2 BYTES`  | integer  | Size of Metadata Entries |
|            | set      | Set of Metadata Entries |

####Metadata Entry
| Size       | Type     | Description |
|------------|----------|-------------|
| `1 BYTE`   | integer  | Length of key string in bytes |
| `1 BYTE`   | integer  | Length of value string in bytes |
| `k BYTES`  | string   | Key string |
| `v BYTES`  | string   | Value string |

###Trash Index
| Size       | Type     | Description |
|------------|----------|-------------|
| `4 BYTES`  | integer  | Number of Trash Entries |
|            | set      | Set of Trash Entries |
| `4 BYTES`  | integer  | Guard bytes `0x54525348` |

####Trash Index Entry
| Size       | Type     | Description |
|------------|----------|-------------|
| `WIDE`     | integer  | Trash section start offset |
| `4 BYTES`  | integer  | Trash section length |


##Appendix

### Flags
| Name               | Value      | Description |
|--------------------|------------|-------------|
| `USE_LONG_OFFSETS` | 0x00000001 | Indicates that this PPAC uses signed 64-bit integers to encode offsets. |
| `JAVA_ARRAY_COMPAT` | 0x00000002 | Indicates that no subfile in this PPAC exceeds 2 GB and may be stored in a Java array. |
