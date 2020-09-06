package com.apkzube.blindf.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

/**
 * Static methods for dealing with {@link android.content.ClipData}.
 */
public final class ClipDatas {

   
    public static void clipPainText(Context context, String label, CharSequence text) {
        final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        final ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
    }

    // Non instantiability.
    private ClipDatas() {
    }
}
