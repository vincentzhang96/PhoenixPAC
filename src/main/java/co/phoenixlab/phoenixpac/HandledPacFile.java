package co.phoenixlab.phoenixpac;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class HandledPacFile<T extends AssetHandle> extends PacFile{

    protected final LinkedHashMap<TypePurposeUniqueId, T> handles = new LinkedHashMap<>();

    public HandledPacFile() {
        super();
    }

    public HandledPacFile(HandledPacFile<T> other) {
        super(other);
        handles.clear();
        deepCopyHandles(other.handles);
    }

    public HandledPacFile(int major, int minor) {
        super(major, minor);
    }

    @SuppressWarnings("unchecked")
    private void deepCopyHandles(Map<TypePurposeUniqueId, T> other) {
        for (Map.Entry<TypePurposeUniqueId, T> entry : other.entrySet()) {
            handles.put(entry.getKey(), (T) entry.getValue().copy());
        }
    }

    public LinkedHashMap<TypePurposeUniqueId, T> getHandles() {
        return handles;
    }

    @Override
    public AssetHandle getHandle(TypePurposeUniqueId tpuid) throws IOException {
        AssetHandle handle = this.handles.get(tpuid);
        if(handle != null) {
            return handle;
        } else {
            throw new FileNotFoundException(tpuid.toString());
        }
    }
}
