package ru.sbrf.wallet.auth;

import android.text.TextUtils;

import ru.sbrf.wallet.data.DataCache;
import ru.sbrf.wallet.models.Session;

public class AccessChecker {
    public void check() throws AuthException {
        if (!sessionOK()) {
            throw new AuthException();
        }
    }

    public boolean sessionOK() {
        final Session s = DataCache.getInstance().getSession();
        return s != null && !TextUtils.isEmpty(s.getSessionId());
    }
}
