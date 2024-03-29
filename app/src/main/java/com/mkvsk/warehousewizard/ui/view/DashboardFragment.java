package com.mkvsk.warehousewizard.ui.view;

import static com.mkvsk.warehousewizard.ui.util.Constants.SP_TAG_PASSWORD;
import static com.mkvsk.warehousewizard.ui.util.Constants.SP_TAG_USERNAME;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.mkvsk.warehousewizard.MainActivity;
import com.mkvsk.warehousewizard.R;
import com.mkvsk.warehousewizard.core.Product;
import com.mkvsk.warehousewizard.core.User;
import com.mkvsk.warehousewizard.databinding.FragmentDashboardBinding;
import com.mkvsk.warehousewizard.ui.util.CustomAlertDialogBuilder;
import com.mkvsk.warehousewizard.ui.viewmodel.ProductViewModel;
import com.mkvsk.warehousewizard.ui.viewmodel.UserViewModel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import kotlin.Pair;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;

    private final List<Product> products = new ArrayList<>();

    // category - qty:price
    private final TreeMap<String, Pair<Long, Double>> filteredData = new TreeMap<>();

    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private ProductViewModel productViewModel;
    private UserViewModel userViewModel;
    private User user = new User();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        productViewModel.setAllProductsFromDB();
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupMenu();
        initListeners();
        initObservers();
    }

    private void initObservers() {
        loadData();
        productViewModel.getAllProducts().observe(getViewLifecycleOwner(), this::filterData);
        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), currentUser -> user = currentUser);
    }

    private void filterData(List<Product> products) {
        final TreeMap<String, List<Product>> categoryMap = new TreeMap<>();
        for (Product product : products) {
            if (categoryMap.containsKey(product.getCategory())) {
                List<Product> list = categoryMap.get(product.getCategory());
                if (list != null) {
                    list.add(product);
                }
            } else {
                List<Product> list = new ArrayList<>();
                list.add(product);
                categoryMap.put(product.getCategory(), list);
            }
        }
        filteredData.clear();
        for (String k : categoryMap.keySet()) {
            List<Product> list = categoryMap.get(k);
            if (list != null) {
                long qtyTotal = list.stream().mapToLong(Product::getQty).sum();
                double priceTotal = list.stream().mapToDouble(product -> product.getQty() * product.getPrice()).sum();
                String format = decimalFormat.format(priceTotal);
                filteredData.put(k, new Pair<>(qtyTotal, Double.parseDouble(format.replace(",", "."))));
            }
        }
        drawData();
    }

    private void drawData() {
        binding.textSumTotal.setText(String.format("Общая стоимость товаров на складе: %s BYN", decimalFormat.format(products.stream().mapToDouble(product -> product.getQty() * product.getPrice()).sum())));
        fillChart();
    }

    private void setupMenu() {
        MenuHost menuHost = binding.toolbar;
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.dashboard_actionbar_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_item_logout) {
                    logout();
                } else if (menuItem.getItemId() == R.id.menu_item_user_profile) {
                    showUserInfo();
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void showUserInfo() {
        CustomAlertDialogBuilder.cardUserInfo(getContext(), user, userResult -> userViewModel.updateUser(userResult)).show();
    }

    private void initListeners() {

    }

    private void logout() {
        SharedPreferences sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().apply();

        editor.putString(SP_TAG_USERNAME, "").apply();
        editor.putString(SP_TAG_PASSWORD, "").apply();

        requireActivity().startActivity(new Intent(requireContext(), MainActivity.class));
        requireActivity().finish();
    }

    private void fillChart() {
        PieChart chart = binding.piechart;
        ArrayList<PieEntry> entries = new ArrayList<>();
        filteredData.forEach((k, v) -> entries.add(new PieEntry(Float.parseFloat(v.getFirst().toString()), k)));
        ArrayList<Integer> colors = new ArrayList<>();

        for (int color : ColorTemplate.PASTEL_COLORS) {
            colors.add(color);
        }
        for (int color : ColorTemplate.PASTEL_COLORS) {
            colors.add(color);
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setDrawValues(true);
        data.setValueFormatter(new PercentFormatter(chart));
        data.setValueTextSize(14f);
        data.setValueTextColor(getContext().getColor(R.color.text_primary_color));

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        l.setTextSize(13f);
        l.setTextColor(getContext().getColor(R.color.text_primary_color));
        chart.setEntryLabelTextSize(0f);
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                // TODO: show value
                String categoryTitle = ((PieEntry) e).getLabel();
                Pair<Long, Double> pair = filteredData.get(categoryTitle);
                if (pair != null) {
                    binding.textCategoryInfo.setText(String.format("Категория: %s\nКоличество: %s\nОбщая стоимость: %s BYN", categoryTitle, pair.getFirst(), pair.getSecond()));
                }
            }

            @Override
            public void onNothingSelected() {
                binding.textCategoryInfo.setText("");
            }
        });
        chart.setUsePercentValues(true);
        chart.setCenterText("");
        chart.setCenterTextSize(12f);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(10, 10, 10, 20);
        chart.setCenterTextColor(getContext().getColor(R.color.text_primary_color));
        chart.setHoleRadius(50f);
        chart.setTransparentCircleRadius(55f);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.setData(data);
        chart.invalidate();
        chart.animateY(1400, Easing.EaseInOutQuad);
        //        loader stop
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadData() {
        products.clear();

        products.addAll(Objects.requireNonNull(productViewModel.getAllProducts().getValue()));
//        String cat0 = "Макияж для лица";
//        String cat1 = "Парфюмерия для мужчин";
//        String cat2 = "Уход за телом";
//        String cat3 = "Уход за лицом";
//        String cat4 = "Для бритья";
//
//        Product p0 = new Product(0, cat0, "title0", "0", 10, "https://bit.ly/3SGyDfX", "desc1", "test", 10.2);
//        Product p1 = new Product(1, cat0, "title1", "1", 21, "https://bit.ly/3SGyDfX", "desc1", "test", 23.3);
//        Product p2 = new Product(2, cat0, "title2", "2", 11, "https://bit.ly/3SGyDfX", "desc1", "test", 23.3);
//        Product p3 = new Product(3, cat0, "title3", "3", 3, "https://bit.ly/3SGyDfX", "desc1", "test", 44.0);
//        Product p4 = new Product(4, cat1, "title4", "4", 22, "https://bit.ly/3SGyDfX", "desc1", "test", 2.22);
//        Product p5 = new Product(5, cat1, "title5", "5", 7, "https://bit.ly/3SGyDfX", "desc1", "test", 5.3);
//        Product p6 = new Product(6, cat2, "title6", "6", 6, "https://bit.ly/3SGyDfX", "desc1", "test", 4.2);
//        Product p7 = new Product(7, cat3, "title7", "7", 5, "https://bit.ly/3SGyDfX", "desc1", "test", 4.3);
//        Product p8 = new Product(8, cat4, "title8", "8", 3, "https://bit.ly/3SGyDfX", "desc1", "test", 663.0);
//        Product p9 = new Product(9, cat4, "title9", "9", 4, "https://bit.ly/3SGyDfX", "desc1", "test", 12.3);
//        products.add(p0);
//        products.add(p1);
//        products.add(p2);
//        products.add(p3);
//        products.add(p4);
//        products.add(p5);
//        products.add(p6);
//        products.add(p7);
//        products.add(p8);
//        products.add(p9);

//        filterData(products);
    }

}

