package net.tiffit.tconplanner.api;

import slimeknights.tconstruct.library.tools.layout.LayoutSlot;

public class TCSlotPos {
    public static final int partsOffsetX = 13, partsOffsetY = 15;

    private final int rawX, rawY;

    TCSlotPos(LayoutSlot pos){
        this(pos.getX(), pos.getY());
    }

    /** Direct position, used for items (armor/shields) that have no station slot layout to source positions from. */
    TCSlotPos(int x, int y){
        this.rawX = x;
        this.rawY = y;
    }

    public int getX(){
        return rawX + partsOffsetX;
    }

    public int getY(){
        return rawY + partsOffsetY;
    }

}
