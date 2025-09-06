package ru.mitriyf.jparkour.utils.schematic.versions;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;
import ru.mitriyf.jparkour.utils.schematic.Paste;

import java.io.File;
import java.nio.file.Files;

public class Paste13 implements Paste {
    @Override
    public void paste(Location loc, File schem) throws Exception {
        ClipboardFormat clipboardFormat = ClipboardFormats.findByFile(schem);
        Clipboard clipboard;
        BlockVector3 blockVector3 = BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        if (clipboardFormat != null) {
            try (ClipboardReader clipboardReader = clipboardFormat.getReader(Files.newInputStream(schem.toPath()))) {
                com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(loc.getWorld());
                EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(world).build();
                clipboard = clipboardReader.read();
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(blockVector3)
                        .ignoreAirBlocks(true)
                        .build();
                Operations.complete(operation);
                editSession.close();
            }
        }
    }
}
