package com.bignerdranch.android.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.text.format.DateFormat;

import java.util.List;

public class CrimeListFragment extends Fragment {

    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    private static final String TAG = "CriminalIntent";
    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private int mCurrentPosition;
    private boolean mSubtitleVisible;

    private View mLayout;
    private Button mAddCrimeButton;

    private Callbacks mCallbacks;

    /**
     * Обязательный интерфейс для активности-хоста.
     */
    public interface Callbacks {
        void onCrimeSelected(Crime crime);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);
        mCrimeRecyclerView = (RecyclerView) view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mLayout = view.findViewById(R.id.add_crime_view);
            mAddCrimeButton = view.findViewById(R.id.add_crime);
            mAddCrimeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addCrime();
                }
            });

        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        updateUI(mCurrentPosition);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI(mCurrentPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }


    public void updateUI(int position) {

        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();
        mLayout.setVisibility(crimes.size() > 0? View.GONE : View.VISIBLE);
        System.out.println("Znachenie ==== " + crimes);
        if (crimes.isEmpty()) {
            System.out.println("Znachenie " + crimes + " NULLLL");
        }
            if (mAdapter == null) {
                mAdapter = new CrimeAdapter(crimes);
                mCrimeRecyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.setCrimes(crimes);
                mAdapter.notifyDataSetChanged();
                Log.e(TAG, "updateUI(int)");
                mAdapter.notifyItemChanged(position);
            }

            updateSubtitle();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_crime:
                addCrime();
                return true;
            case R.id.show_subtitle:
                System.out.println("Znachenie!!! ==== " + mSubtitleVisible);
                mSubtitleVisible = !mSubtitleVisible;
                System.out.println(mSubtitleVisible);
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addCrime() {
        Crime crime = new Crime();
        CrimeLab.get(getActivity()).addCrime(crime);
        updateUI(mCurrentPosition);
        mCallbacks.onCrimeSelected(crime);
    }

    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getString(R.string.subtitle_format, crimeCount);

        if (!mSubtitleVisible) {
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    private abstract class AbstractCrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private ImageView mSolvedImageView;


        public AbstractCrimeHolder(LayoutInflater inflater, ViewGroup parent, int layoutId) {
            super(inflater.inflate(layoutId, parent, false));

            itemView.setOnClickListener(this);

            mTitleTextView = (TextView) itemView.findViewById(R.id.crime_title);
            mDateTextView = (TextView) itemView.findViewById(R.id.crime_date);
            mSolvedImageView = (ImageView) itemView.findViewById(R.id.crime_solved);
        }

        private Crime mCrime;

        public void bind(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(DateFormat.format("E, d MMM, yyyy", mCrime.getDate()));
//            mDateTextView.setText(mCrime.getDate().toString());
            mSolvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE : View.GONE);
        }

        public Crime getCrime() {
            return mCrime;
        }

        @Override
        public void onClick(View view) {
          mCurrentPosition = getAdapterPosition();
            mCallbacks.onCrimeSelected(mCrime);
        }
    }

    private class CrimeHolder extends AbstractCrimeHolder {
        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater, parent, R.layout.list_item_crime);
        }
    }

    private class PoliceCrimeHolder extends AbstractCrimeHolder {
        public PoliceCrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater, parent, R.layout.list_item_crime_police);

            Button policeButton = (Button)itemView.findViewById(R.id.contact_police_button);
            policeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(),"Полиция вызвана по преступлению: " + getCrime().getTitle(), Toast.LENGTH_SHORT)
                    .show();
                }
            });
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

//        private static final int LIST_ITEM_CRIME = 0;
//        private static final int LIST_ITEM_CRIME_POLICE = 1;

        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            if (viewType == R.layout.list_item_crime) {
                return new CrimeHolder(layoutInflater, parent);
            } else if (viewType == R.layout.list_item_crime_police) {
                return new PoliceCrimeHolder(layoutInflater, parent);
            } else {
                return null;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            if (holder instanceof CrimeHolder) {
                ((CrimeHolder) holder).bind(crime);
            } else if (holder instanceof PoliceCrimeHolder) {
                ((PoliceCrimeHolder) holder).bind(crime);
            }
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }
///
        @Override
        public int getItemViewType (int position) {
            boolean requiresPolice = mCrimes.get(position).isRequiresPolice();
            if (requiresPolice) {
                return R.layout.list_item_crime_police; //2131296290
            } else {
                return R.layout.list_item_crime;
            }

        }
    }
}