package jna;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface TclLib extends Library {
    TclLib INSTANCE = Native.load("tcl", TclLib.class);

    int SetClipboardUTF8(String text);
}
