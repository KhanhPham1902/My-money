package com.example.mymoney.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mymoney.R;
import com.example.mymoney.databinding.ActivityMainBinding;
import com.example.mymoney.fragment.ChartFragment;
import com.example.mymoney.fragment.HomeFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment(new HomeFragment());

        binding.bottomView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navHome) {
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.navChart) {
                replaceFragment(new ChartFragment());
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}