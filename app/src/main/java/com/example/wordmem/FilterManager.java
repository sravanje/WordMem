package com.example.wordmem;

public class FilterManager {
    public static class FilterSettings {
        public boolean isFiltered = false;
        public String filterType = "all"; // "all", "category", "range"
        public String category = "all words";
        public double minScore = 0.0;
        public double maxScore = 1.0;
        
        public void reset() {
            isFiltered = false;
            filterType = "all";
            category = "all words";
            minScore = 0.0;
            maxScore = 1.0;
        }
        
        public String getDisplayText() {
            if (!isFiltered) {
                return "All Words";
            }
            
            if (filterType.equals("category")) {
                return category;
            } else if (filterType.equals("range")) {
                return String.format("Score: %.0f%% - %.0f%%", minScore * 100, maxScore * 100);
            }
            
            return "All Words";
        }
    }
    
    private static FilterSettings currentFilter = new FilterSettings();
    
    public static FilterSettings getCurrentFilter() {
        return currentFilter;
    }
    
    public static void setFilter(FilterSettings filter) {
        currentFilter = filter;
    }
    
    public static void clearFilter() {
        currentFilter.reset();
    }
}
