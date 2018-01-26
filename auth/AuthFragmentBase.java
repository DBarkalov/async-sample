package ru.sbrf.wallet.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import ru.sbrf.wallet.registration.RegisterActivity;

public class AuthFragmentBase extends Fragment implements AuthDelegate {
    private AuthDelegate mAuthDelegate;

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
    public void runAction(AuthAction action) {
        mAuthDelegate.runAction(action);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RegisterActivity.RESET_PASSWORD_REQUEST_CODE &&
                resultCode == Activity.RESULT_OK) {
            onRegistrationCompleteOK();
        }
    }

   
}
