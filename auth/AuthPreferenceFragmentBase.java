package ru.sbrf.wallet.auth;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceFragment;

// TODO перейти на PreferenceFragmentCompat из com.android.support:preference-v14, фиксит много мелких проблем
public class AuthPreferenceFragmentBase extends PreferenceFragment implements AuthDelegate {

    private AuthDelegate mAuthDelegate;

    // FIXME не вызывается в API ниже 23, исправленно в PreferenceFragmentCompat
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mAuthDelegate = (AuthDelegate) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().getSimpleName() +
                    " must implement " + AuthDelegate.class.getSimpleName());
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mAuthDelegate = (AuthDelegate) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.getClass().getSimpleName() +
                    " must implement " + AuthDelegate.class.getSimpleName());
        }
    }

    @Override
    public void runAction(AuthAction action) {
        mAuthDelegate.runAction(action);
    }
}
