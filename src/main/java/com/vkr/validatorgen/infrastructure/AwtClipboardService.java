package com.vkr.validatorgen.infrastructure;

import com.vkr.validatorgen.domain.ClipboardService;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

public final class AwtClipboardService implements ClipboardService {
    @Override
    public void copy(String text) {
        if (text == null) text = "";
        var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(text), null);
    }
}
