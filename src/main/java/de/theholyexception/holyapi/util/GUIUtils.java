package de.theholyexception.holyapi.util;

import java.awt.*;
import java.io.IOException;

public class GUIUtils {

    /**
     * It takes a number of bytes and returns a string with the number of bytes in a human readable format
     *
     * @param len The length of the file in bytes.
     * @return A string representing the size of the file in bytes, kilobytes, megabytes, gigabytes, terabytes, petabytes,
     * or exabytes.
     */
    public static String formatStorageSpace(long len) {
        int steps = 0;
        String[] args = new String[] { "B", "KB", "MB", "GB", "TB", "PB", "EB" };
        long last = 0;
        while (len >= 1024 && steps + 1 < args.length) {
            steps++;
            last = len % 1024;
            len >>= 10;
        }
        String a = String.valueOf(len);
        String b = "";
        if(steps > 0) {
            double d = (double) last / 1024;
            int l = 4 - a.length();
            l = l < 0 ? 0 : l;
            if (l >= 1)
                b = "." + ((int) (d * 10) % 10);
            if (l >= 2)
                b += ((int) (d * 100) % 10);
            if (l >= 3)
                b += ((int) (d * 1000) % 10);
        }
        return a + b + " " + args[steps];
    }

    /**
     * It adds a component to a container using a GridBagLayout
     *
     * @param component The component to add to the container.
     * @param base The container to which the component will be added.
     * @param gridx The x coordinate of the cell in the grid.
     * @param gridy The row position of the component.
     * @param gridwidth The number of columns the component will take up.
     * @param gridheight The number of cells in a column for the component's display area.
     * @param weigthx The weight of the component in the x direction.
     * @param weigthy The weighty parameter is used to determine how to distribute space among rows on the vertical axis (y
     * axis).
     * @param insets The space between the component and the edges of its display area.
     */
    public static void addGBComponent(Component component, Container base, int gridx, int gridy, int gridwidth, int gridheight, float weigthx, float weigthy, Insets insets) {
        base.add(component, new GridBagConstraints(
                gridx,
                gridy,
                gridwidth,
                gridheight,
                weigthx,
                weigthy,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                insets,
                0,
                0));
    }

    /**
     * "Add a component to a container using GridBagLayout, with the specified gridx, gridy, gridwidth, gridheight, and
     * insets."
     *
     * The function is a static method, so it can be called without creating an instance of the class
     *
     * @param component The component to add to the container.
     * @param base The container to add the component to.
     * @param gridx The x coordinate of the cell in the grid.
     * @param gridy The row position of the component.
     * @param gridwidth The number of columns the component will take up.
     * @param gridheight The number of cells in a column for the component's display area.
     * @param insets The insets of the component.
     */
    public static void addGBComponent(Component component, Container base, int gridx, int gridy, int gridwidth, int gridheight, Insets insets) {
        base.add(component, new GridBagConstraints(
                gridx,
                gridy,
                gridwidth,
                gridheight,
                1.0,
                1.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                insets,
                0,
                0));
    }

}
