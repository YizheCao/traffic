package com.example.traffic;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button btnViewProducts;
    Button btnNewProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnViewProducts = (Button) findViewById(R.id.btnViewProducts);
        btnNewProduct = (Button) findViewById(R.id.btnCreateProduct);

        btnViewProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launching All products Activity
                Intent i = new Intent(getApplicationContext(), show.class);
                startActivity(i);
            }
        });

        btnNewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launching create new product activity
                Intent i = new Intent(getApplicationContext(), upload.class);
                startActivity(i);
            }
        });
    }
}
