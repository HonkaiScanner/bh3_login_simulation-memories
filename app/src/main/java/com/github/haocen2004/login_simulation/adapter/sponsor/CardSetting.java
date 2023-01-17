package com.github.haocen2004.login_simulation.adapter.sponsor;

import androidx.annotation.NonNull;

import com.drakeet.about.Card;
import com.github.haocen2004.login_simulation.data.ICallback;

public class CardSetting extends Card {
    public @NonNull
    final CharSequence content;
    public final int lineSpacingExtra;
    public ICallback onClick;
    public ICallback onLongClick;

    public CardSetting(@NonNull CharSequence content, ICallback param) {
        this(content, param, null, 0);
    }

    public CardSetting(@NonNull CharSequence content, ICallback param, ICallback param2) {
        this(content, param, param2, 0);
    }

    public CardSetting(@NonNull CharSequence content, ICallback onClick, ICallback onLongClick, int lineSpacingExtra) {
        super(content, lineSpacingExtra);
        this.content = content;
        this.onClick = onClick;
        this.onLongClick = onLongClick;
        this.lineSpacingExtra = lineSpacingExtra;
    }
}
