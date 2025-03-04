package de.theholyexception.holyapi.util.drophandler;

import de.theholyexception.holyapi.util.logger.LogLevel;
import de.theholyexception.holyapi.util.logger.LoggerProxy;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.util.List;

public class DropHandler implements DropTargetListener {

    private final Component component;
    private final DropAction action;
    private int dropPosX;
    private int dropPosY;

    public DropHandler(Component component, DropAction action) {
        this.component = component;
        this.action = action;
        new DropTarget(component, this);
    }

    @Override
    public void dragEnter(DropTargetDragEvent event) {
        if (!isDragAcceptable(event)) {
            event.rejectDrag();
        }
    }

    @Override
    public void dragExit(DropTargetEvent event) {
    }

    @Override
    public void dragOver(DropTargetDragEvent event) {
        dropPosX = (int)event.getLocation().getX();
        dropPosY = (int)event.getLocation().getY();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void drop(DropTargetDropEvent event) {
        event.acceptDrop(DnDConstants.ACTION_MOVE);
        Transferable transferable = event.getTransferable();
        DataFlavor[] flavors = transferable.getTransferDataFlavors();
        for (int i = 0; i < flavors.length; i++) {
            DataFlavor d = flavors[i];
            try {
                System.out.println(transferable.getTransferData(d));
                if (d.equals(DataFlavor.javaFileListFlavor)) {
                    List<File> fileList = (List<File>) transferable.getTransferData(d);
                    action.apply(new DropInformation(fileList, dropPosX, dropPosY));
                }
            }catch(Exception e){
                LoggerProxy.log(LogLevel.ERROR, "Failed to drop file", e);
            }
        }
        event.dropComplete(true);
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent event) {
        if (!isDragAcceptable(event)) {
            event.rejectDrag();
        }
    }

    public boolean isDragAcceptable(DropTargetDragEvent event) {
        return (event.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) != 0;
    }
}
