package net.tiffit.tconplanner.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlannerData {

    public final List<Blueprint> saved = new ArrayList<>();
    public Blueprint starred;

    private final File bookmarkFile;
    private boolean hasLoaded;

    public PlannerData(File folder){
        bookmarkFile = new File(folder, "bookmark.dat");
        try {
            //noinspection ResultOfMethodCallIgnored
            folder.mkdir();
            if (!bookmarkFile.exists()) {
                assert bookmarkFile.createNewFile();
                save();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void refresh() throws IOException {
        save();
        load();
    }

    public boolean isBookmarked(Blueprint bp){
        return saved.stream().anyMatch(blueprint -> blueprint.equals(bp));
    }

    public void save() throws IOException {
        NbtIo.writeCompressed(toNBT(), bookmarkFile.toPath());
    }

    /** Serializes the current bookmarks to a self-contained tag. Also used as the blob synced to/from the server. */
    public CompoundTag toNBT() {
        ListTag nbt = new ListTag();
        List<CompoundTag> others = new ArrayList<>();
        for (Blueprint bp : saved) {
            CompoundTag cnbt = bp.toNBT();
            if(bp.isComplete() && !others.contains(cnbt)){
                nbt.add(cnbt);
                others.add(cnbt);
            }
        }
        CompoundTag data = new CompoundTag();
        data.put("list", nbt);
        if(starred != null && starred.isComplete()){
            data.put("starred", starred.toNBT());
        }
        return data;
    }

    public boolean isEmpty() {
        return saved.isEmpty() && starred == null;
    }

    public void firstLoad() throws IOException {
        if(!hasLoaded) {
            load();
        }
    }

    public void load() throws IOException {
        loadFromNBT(NbtIo.readCompressed(bookmarkFile.toPath(), NbtAccounter.unlimitedHeap()));
    }

    /** Loads bookmarks from a tag, whether read from the local file or received from the server. */
    public void loadFromNBT(CompoundTag data) {
        hasLoaded = true;
        saved.clear();
        ListTag nbt = data.getList("list", Tag.TAG_COMPOUND);
        for(int i = 0; i < nbt.size(); i++){
            saved.add(Blueprint.fromNBT(nbt.getCompound(i)));
        }
        saved.removeIf(Objects::isNull);
        if(data.contains("starred")){
            starred = Blueprint.fromNBT(data.getCompound("starred"));
        }else{
            starred = null;
        }
    }

}
