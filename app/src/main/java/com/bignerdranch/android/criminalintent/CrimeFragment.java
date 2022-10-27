package com.bignerdranch.android.criminalintent;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.widget.CompoundButton.*;

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TITLE = "DialogTitle";
    private static final String DIALOG_PHOTO = "DialogPhoto";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TITLE = 1;
    private static final int REQUEST_CONTACT = 2;
    private static final int REQUEST_PHOTO= 3;

    private static final int REQUEST_CONTACTS_PERMISSIONS = 0;
    private static final String[] CONTACTS_PERMISSIONS = new String[]{Manifest.permission.READ_CONTACTS,};

    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private CheckBox mRequiredPoliceCheckBox;
    private Button mReportButton;
    private Button mSuspectButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private File mPhotoFile;
    private String mSuspectId;
    private Button mCallSuspectButton;

    private Callbacks mCallbacks;
    /**
     * Необходимый интерфейс для активности-хоста.
     */
    public interface Callbacks {
        void onCrimeUpdated(Crime crime);
    }

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
//        mCrime = new Crime();
//        UUID crimeId = (UUID) getActivity().getIntent().getSerializableExtra(MainActivity.EXTRA_CRIME_ID);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);
        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                updateCrime();
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton = (Button) v.findViewById(R.id.crime_date);
        updateDate();
//        mDateButton.setText(DateFormat.format("E, d MMM, yyyy", mCrime.getDate()));
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                        .newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                updateCrime();
            }
        });

        mRequiredPoliceCheckBox = (CheckBox) v.findViewById(R.id.requires_police);
        mRequiredPoliceCheckBox.setChecked(mCrime.isRequiresPolice());
        mRequiredPoliceCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setRequiresPolice(isChecked);
                updateCrime();
            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                Intent shareIntent = ShareCompat.IntentBuilder.from(getActivity())
//                        .setType("text/plain")
//                        .setText(getCrimeReport())
//                        .setSubject(getString(R.string.crime_report_subject))
//                        .getIntent();

                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.crime_report_subject));
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                                                ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });
        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }

        final Intent callContact = new Intent(Intent.ACTION_DIAL);
        mCallSuspectButton = (Button) v.findViewById(R.id.crime_suspect_phone);
        mCallSuspectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Uri number = Uri.parse("tel:" + mCrime.getPhone()); // the “tel:” is needed to start activity
                callContact.setData(number);
//                Intent callContact = new Intent(Intent.ACTION_DIAL, number);
                startActivity(callContact);
            }
        });
        if (mCrime.getPhone() != null) {
            mCallSuspectButton.setText("Позвонить: " + mCrime.getPhone());
        } else {
            mCallSuspectButton.setEnabled(false);
            mCallSuspectButton.setText("Нет номера для звонка");
        }
//---------------------------------------------------------------------------------------------------------------------
        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "com.bignerdranch.android.criminalintent.fileprovider",
                        mPhotoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                List<ResolveInfo> cameraActivities = getActivity()
                        .getPackageManager().queryIntentActivities(captureImage,
                                PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo activity : cameraActivities) {
                    getActivity().grantUriPermission(activity.activityInfo.packageName,
                    uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });
        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);
        updatePhotoView();

        mPhotoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                PhotoViewerDialog dialog = PhotoViewerDialog.newInstance(mPhotoFile);
                dialog.show(manager, DIALOG_PHOTO);
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
            updateCrime();
        }
        if (requestCode == REQUEST_TITLE) {
            CrimeLab.get(getActivity()).deleteCrime(mCrime);
            getActivity().finish();
        }

        if (requestCode == REQUEST_CONTACT && data != null) {

            String suspectName = getSuspectName(data);
            mCrime.setSuspect(suspectName);
            updateCrime();
            mSuspectButton.setText(suspectName);

            // Get the suspect's mobile phone (cell phone) number.
            if (hasContactPermission()) {
                System.out.println("222222222222222222>>>>>>>> " + mCrime.getPhone());
                updateSuspectPhone();
                if (mCrime.getPhone() != null) {
                    mCallSuspectButton.setEnabled(true);
                    mCallSuspectButton.setText("Позвонить: " + mCrime.getPhone());
                } else {
                    mCallSuspectButton.setEnabled(false);
                    mCallSuspectButton.setText("Нет номера для звонка");
                }
            } else {
                // This will call onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults).
                requestPermissions(CONTACTS_PERMISSIONS, REQUEST_CONTACTS_PERMISSIONS);
            }
        }

        if (requestCode == REQUEST_PHOTO) {
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "com.bignerdranch.android.criminalintent.fileprovider",
                    mPhotoFile);
            getActivity().revokeUriPermission(uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            updateCrime();
            updatePhotoView();
        }
    }

    private String getSuspectName(Intent data) {
        Uri contactUri = data.getData();

        // Определение полей, значения которых должны быть возвращены запросом.
        String[] queryFields = new String[] {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
        };

        // Выполнение запроса - contactUri здесь выполняет функции условия "where"
        Cursor c = getActivity().getContentResolver()
                .query(contactUri, queryFields, null, null, null);

        try {
            // Проверка получения результатов
            if (c.getCount() == 0) {
                return null;
            }
            // Извлечение первого столбца данных - имени подозреваемого.
            c.moveToFirst();

            mSuspectId = c.getString(0);
            String suspectName = c.getString(1);
            return suspectName;
        } finally {
            c.close();
        }
    }

    private String getSuspectPhoneNumber(String contactId) {
        String suspectPhoneNumber = null;

        // The content URI of the CommonDataKinds.Phone
        Uri phoneContactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        // The columns to return for each row
        String[] queryFields = new String[] {
                ContactsContract.Data.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,   // which is the default phone number.
                ContactsContract.CommonDataKinds.Phone.TYPE,
        };

        // Selection criteria
        String selectionClause = ContactsContract.Data.CONTACT_ID + " = ?";

        // Selection criteria
        String[] selectionArgs = {""};
        selectionArgs[0] = contactId;

        // Does a query against the table and returns a Cursor object
        Cursor c = getActivity().getContentResolver()
                .query(phoneContactUri,queryFields, selectionClause, selectionArgs, null );

        try {
            // Double-check that you actually got results.
            if (c.getCount() == 0) {
                return null;
            }

            while (c.moveToNext()) {
                int phoneType = c.getInt(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                if (phoneType == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                    suspectPhoneNumber = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
                    break;
                }
            }
        } finally {
            c.close();
        }

        return suspectPhoneNumber;
    }

    private void updateSuspectPhone () {
        String suspectPhoneNumber = getSuspectPhoneNumber(mSuspectId);
        mCrime.setPhone(suspectPhoneNumber);
    }

    private boolean hasContactPermission() {
        int result = ContextCompat.checkSelfPermission(getActivity(), CONTACTS_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CONTACTS_PERMISSIONS:
                if (hasContactPermission()) {
                    updateSuspectPhone();
                }
        }
    }
//----------------------------------------------------------------------------------------------------------------

    private void updateCrime() {
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }

    private void updateDate() {
//        mDateButton.setText(mCrime.getDate().toString());
        mDateButton.setText(DateFormat.format("E, d MMM, yyyy", mCrime.getDate()));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.activity_crime_pager, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_crime:
                FragmentManager manager = getFragmentManager();
                DeleteCrimeDialog dialog = DeleteCrimeDialog.newInstance(mCrime.getTitle());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TITLE);
                dialog.show(manager, DIALOG_TITLE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String getCrimeReport() {
        String solvedString = null;
        String requiresPoliceString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }
        if (mCrime.isRequiresPolice()) {
            requiresPoliceString = getString(R.string.crime_report_police);
        } else {
            requiresPoliceString = getString(R.string.crime_report_not_police);
        }
        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat,
                mCrime.getDate()).toString();
        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }
        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspect, requiresPoliceString);
        return report;
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
            //TalkBack
            mPhotoView.setContentDescription(getString(R.string.crime_photo_no_image_description));
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
            //TalkBack
            mPhotoView.setContentDescription(getString(R.string.crime_photo_image_description));
        }
    }
}