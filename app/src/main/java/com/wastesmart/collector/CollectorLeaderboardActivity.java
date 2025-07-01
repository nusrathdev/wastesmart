package com.wastesmart.collector;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.wastesmart.R;
import com.wastesmart.adapters.LeaderboardAdapter;
import com.wastesmart.databinding.ActivityCollectorLeaderboardBinding;
import com.wastesmart.models.Collector;
import com.wastesmart.models.LeaderboardItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CollectorLeaderboardActivity extends BaseCollectorActivity {

    private ActivityCollectorLeaderboardBinding binding;
    private LeaderboardAdapter leaderboardAdapter;
    private List<LeaderboardItem> leaderboardItems;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCollectorLeaderboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Collectors Leaderboard");

        // Setup bottom navigation
        setupBottomNavigation();

        // Initialize list and adapter
        leaderboardItems = new ArrayList<>();
        leaderboardAdapter = new LeaderboardAdapter(this, leaderboardItems);

        // Set up RecyclerView
        binding.rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLeaderboard.setAdapter(leaderboardAdapter);

        // Load leaderboard data
        loadLeaderboardData();
    }

    private void loadLeaderboardData() {
        binding.progressBar.setVisibility(View.VISIBLE);

        db.collection("collectors")
          .orderBy("completedTasks", Query.Direction.DESCENDING)
          .limit(20) // Limit to top 20 collectors
          .get()
          .addOnSuccessListener(queryDocumentSnapshots -> {
              binding.progressBar.setVisibility(View.GONE);
              leaderboardItems.clear();

              int rank = 1;
              for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                  Collector collector = document.toObject(Collector.class);
                  
                  // In a real app, we would have metrics like completedTasks
                  // For now, use a placeholder or random value if not available
                  int score = 0;
                  if (document.contains("completedTasks")) {
                      score = document.getLong("completedTasks").intValue();
                  } else {
                      // Placeholder scores for demonstration
                      score = 100 - (rank * 5) + (int)(Math.random() * 10);
                  }
                  
                  LeaderboardItem item = new LeaderboardItem(
                          document.getId(),
                          collector.getName(),
                          rank,
                          score,
                          collector.getAssignedArea()
                  );
                  leaderboardItems.add(item);
                  rank++;
              }

              // If no data is found, add some sample data
              if (leaderboardItems.isEmpty()) {
                  addSampleLeaderboardData();
              }

              leaderboardAdapter.notifyDataSetChanged();
          })
          .addOnFailureListener(e -> {
              binding.progressBar.setVisibility(View.GONE);
              Toast.makeText(this, "Error loading leaderboard: " + e.getMessage(), Toast.LENGTH_SHORT).show();
              
              // Add sample data if loading fails
              addSampleLeaderboardData();
          });
    }

    private void addSampleLeaderboardData() {
        // Add sample data for demonstration purposes
        leaderboardItems.add(new LeaderboardItem("1", "Sarah Johnson", 1, 98, "Downtown"));
        leaderboardItems.add(new LeaderboardItem("2", "Michael Chen", 2, 95, "Eastside"));
        leaderboardItems.add(new LeaderboardItem("3", "David Wilson", 3, 92, "Westside"));
        leaderboardItems.add(new LeaderboardItem("4", "Emma Rodriguez", 4, 87, "Northside"));
        leaderboardItems.add(new LeaderboardItem("5", "James Lee", 5, 84, "Downtown"));
        leaderboardItems.add(new LeaderboardItem("6", "Olivia Garcia", 6, 81, "Southside"));
        leaderboardItems.add(new LeaderboardItem("7", "William Brown", 7, 79, "Eastside"));
        leaderboardItems.add(new LeaderboardItem("8", "Sophia Martinez", 8, 76, "Downtown"));
        leaderboardItems.add(new LeaderboardItem("9", "Benjamin Clark", 9, 74, "Westside"));
        leaderboardItems.add(new LeaderboardItem("10", "Isabella Scott", 10, 70, "Northside"));
        
        leaderboardAdapter.notifyDataSetChanged();
    }

    @Override
    protected int getActiveNavItemIndex() {
        return 4; // Leaderboard tab index
    }
}
