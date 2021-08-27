/*
 * Nextcloud Geofavorites for Android
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.danieleverducci.nextcloudmaps.activity.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.exceptions.AccountImportCancelledException;
import com.nextcloud.android.sso.exceptions.AndroidGetAccountsPermissionNotGranted;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotInstalledException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;
import com.nextcloud.android.sso.ui.UiExceptionManager;

import it.danieleverducci.nextcloudmaps.R;
import it.danieleverducci.nextcloudmaps.activity.main.MainActivity;
import it.danieleverducci.nextcloudmaps.api.ApiProvider;

public class LoginActivity extends AppCompatActivity {

    protected ApiProvider mApi;
    protected ProgressBar progress;
    protected Button button;

    protected SingleSignOnAccount ssoAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progress = findViewById(R.id.progress);
        button = findViewById(R.id.chose_button);
        button.setOnClickListener(view -> {
            progress.setVisibility(View.VISIBLE);
            openAccountChooser();
        });

        try {
            ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(getApplicationContext());
            SingleAccountHelper.setCurrentAccount(getApplicationContext(), ssoAccount.name);
            accountAccessDone();
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
        }
    }
    private void openAccountChooser() {
        try {
            AccountImporter.pickNewAccount(this);
        } catch (NextcloudFilesAppNotInstalledException | AndroidGetAccountsPermissionNotGranted e) {
            UiExceptionManager.showDialogForException(this, e);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            AccountImporter.onActivityResult(requestCode, resultCode, data, this, new AccountImporter.IAccountAccessGranted() {
                NextcloudAPI.ApiConnectedListener callback = new NextcloudAPI.ApiConnectedListener() {
                    @Override
                    public void onConnected() {
                        // ignore this one… see 5)
                    }

                    @Override
                    public void onError(Exception ex) {
                        // TODO handle errors
                    }
                };

                @Override
                public void accountAccessGranted(SingleSignOnAccount account) {
                    Context l_context = getApplicationContext();
                    SingleAccountHelper.setCurrentAccount(l_context, account.name);

                    accountAccessDone();
                }
            });
        } catch (AccountImportCancelledException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AccountImporter.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void accountAccessDone() {
        Context l_context = getApplicationContext();
        mApi = new ApiProvider(l_context);

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);

        finish();
    }
}