package com.devinotele.exampleapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.devinotele.devinosdk.sdk.DevinoLogsCallback;
import com.devinotele.devinosdk.sdk.DevinoSdk;
import com.devinotele.exampleapp.util.BriefTextWatcher;
import com.devinotele.exampleapp.util.Util;

import java.util.Arrays;

public class RegistrationFragment extends Fragment implements View.OnClickListener {

    private EditText userEmail, userPhone, apiBaseUrl;
    private String phone = "";
    private String email = "";
    private String apiUrl = "";
    private static final String SAVED_PHONE = "savedPhone";
    private static final String SAVED_EMAIL = "savedEmail";
    private static final int PHONE_LENGTHS = 12;
    private static final String SAVED_API_URL = "savedPassword";
    private DevinoLogsCallback logsCallback;
    private MainActivityCallback mainActivityCallback;

    public RegistrationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registration, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (!(context instanceof MainActivityCallback)) {
            throw new ClassCastException(getString(R.string.class_cast_exception));
        }
        mainActivityCallback = (MainActivityCallback) context;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        logsCallback = mainActivityCallback.getLogsCallback();

        userPhone = requireView().findViewById(R.id.edt_user_phone);
        userEmail = requireView().findViewById(R.id.edt_user_email);
        apiBaseUrl = requireView().findViewById(R.id.edt_api_url);

        Integer phoneValueLength = userPhone.getText().length();
        userPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) userPhone.setSelection(phoneValueLength);
        });

        userPhone.addTextChangedListener(new BriefTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() < 3) {
                    userPhone.setText(getString(R.string.text_79));
                }
                else if (!s.subSequence(0, 3).toString().equals(getString(R.string.text_79))) {
                    String prefix = s.subSequence(0, 3).toString();
                    String newValue = s.toString().replace(prefix, getString(R.string.text_79));
                    userPhone.setText(newValue);
                }
                if (s.length() > 12) {
                    userPhone.setText(s.subSequence(0, 12));
                }
            }
        });

        apiBaseUrl.setText(getString(R.string.api_base_url));

        if (savedInstanceState != null) {
            phone = savedInstanceState.getString(SAVED_PHONE);
            email = savedInstanceState.getString(SAVED_EMAIL);
            apiUrl = savedInstanceState.getString(SAVED_API_URL);
            userPhone.setText(phone);
            userEmail.setText(email);
            apiBaseUrl.setText(apiUrl);
        }

        TextView skipRegistration = requireView().findViewById(R.id.text_skip_registration);
        Button register = requireView().findViewById(R.id.btn_registration);
        Button confirmBaseUrl = requireView().findViewById(R.id.btn_confirm_root_url);

        register.setOnClickListener(this);
        skipRegistration.setOnClickListener(this);
        confirmBaseUrl.setOnClickListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(SAVED_PHONE, phone);
        outState.putString(SAVED_EMAIL, email);
        outState.putString(SAVED_API_URL, apiUrl);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        Bundle state = new Bundle();
        state.putString(SAVED_PHONE, phone);
        state.putString(SAVED_EMAIL, email);
        state.putString(SAVED_API_URL, apiUrl);
        onSaveInstanceState(state);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        if (v.getId() == R.id.btn_registration) {
            try {
                doRegistration(navController);
            } catch (Exception e) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(e.getLocalizedMessage())
                        .setMessage(Arrays.toString(e.getStackTrace()))
                        .setPositiveButton(android.R.string.yes, null)
                        .show();
            }
        }

        if (v.getId() == R.id.text_skip_registration) {
            try {
                navigateToHomeScreen(navController, false);
            } catch (Exception e) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(e.getLocalizedMessage())
                        .setMessage(Arrays.toString(e.getStackTrace()))
                        .setPositiveButton(android.R.string.yes, null)
                        .show();
            }
        }

        if (v.getId() == R.id.btn_confirm_root_url) {
            try {
                updateBaseApiUrl();
            } catch (Exception e) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(e.getLocalizedMessage())
                        .setMessage(Arrays.toString(e.getStackTrace()))
                        .setPositiveButton(android.R.string.yes, null)
                        .show();
            }
        }
    }

    private void doRegistration(NavController navController) {
        Editable phoneValue = userPhone.getText();
        Editable emailValue = userEmail.getText();
        boolean isPhoneOk = PhoneNumberUtils.isGlobalPhoneNumber(phoneValue.toString())
                && phoneValue.length() == PHONE_LENGTHS;
        boolean isEmailOk = Util.checkEmail(emailValue.toString());

        if (!isEmailOk) {
            userEmail.setBackground(
                    AppCompatResources.getDrawable(
                            requireContext(),
                            R.drawable.ic_border_red)
            );
        }
        if (!isPhoneOk) {
            userPhone.setBackground(
                    AppCompatResources.getDrawable(
                            requireContext(),
                            R.drawable.ic_border_red)
            );
        }

        if (isPhoneOk && isEmailOk)
            try {
                DevinoSdk.getInstance().register(phoneValue.toString(), emailValue.toString());
                navigateToHomeScreen(navController, true);
            } catch (Exception e) {
                Log.d(
                        getString(R.string.logs_tag),
                        getString(R.string.error_registration) + " " + e.getMessage()
                );
            }
        else
            logsCallback.onMessageLogged(getString(R.string.error_phone_email));
    }

    private void navigateToHomeScreen(NavController navController, Boolean isRegisteredUser) {
        RegistrationFragmentDirections.ActionRegistrationFragmentToHomeFragment action =
                RegistrationFragmentDirections.actionRegistrationFragmentToHomeFragment();
        action.setIsRegisteredUser(isRegisteredUser);
        navController.navigate(action);
    }

    private void updateBaseApiUrl() {
        String newBaseApiUrl = apiBaseUrl.getText().toString();
        DevinoSdk.getInstance().updateBaseApiUrl(newBaseApiUrl, requireContext());
    }
}