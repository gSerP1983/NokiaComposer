package com.serp1983.nokiacomposer.lib;

import android.content.Context;

public class ClipboardService {
	@SuppressWarnings("deprecation")
	public static void Copy(Context context, String text, String label){
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			android.content.ClipboardManager clipboard =
					(android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			android.content.ClipData clip = android.content.ClipData.newPlainText(label, text);
			clipboard.setPrimaryClip(clip);
		} else {
			android.text.ClipboardManager clipboard =
					(android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(text);
		}
	}
}
