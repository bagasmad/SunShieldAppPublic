package com.example.sunshieldapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

public class WeatherClass {
    public static Integer kodeCuacaForecast;
    public static Integer kodeCuacaCurrent;
    public static Double indexUV;

    public static void setIndexUV(double UV, Activity activity) {
        WeatherClass.indexUV = UV;
        Log.i("indexUV",Double.toString(indexUV));
        if (kodeCuacaForecast!=null&&kodeCuacaCurrent!=null&&indexUV!=null)
        {
            setRekomendasi(activity);
        }
    }

    public static void setKodeCuacaForecast(int kodeCuaca, Activity activity) {
        WeatherClass.kodeCuacaForecast = kodeCuaca;
        Log.i("kodeCuaca",Integer.toString(kodeCuaca));
        if (kodeCuacaForecast!=null&&kodeCuacaCurrent!=null&&indexUV!=null)
        {
            setRekomendasi(activity);
        }
    }

    public static void setKodeCuacaCurrent(int kodeCuaca, Activity activity) {
        WeatherClass.kodeCuacaCurrent = kodeCuaca;
        Log.i("kodeCuacaCurrent",Integer.toString(kodeCuaca));
        if (kodeCuacaForecast!=null&&kodeCuacaCurrent!=null&&indexUV!=null)
        {
            setRekomendasi(activity);
        }
    }

    public static void setRekomendasi(Activity activity)
    {
        TextView rekomendasiUV = activity.findViewById(R.id.teksRekomendasiUV);
        TextView rekomendasiAktivitas = activity.findViewById(R.id.teksRekomendasiAktivitas);
        TextView rekomendasiAtribut = activity.findViewById(R.id.teksRekomendasiAtribut);
        TextView rekomendasiPrediksi = activity.findViewById(R.id.teksRekomendasiPrediksi);
        if(indexUV<=8)
        {
            if(indexUV>=3)
            {
                rekomendasiUV.setText("Paparan UV sedang, gunakan perlindungan sunscreen bila diperlukan");
                rekomendasiAktivitas.setText("Paparan UV sedang, kurangi aktivitas di luar ruangan, gunakan sunscreen secara berkala");
                rekomendasiAtribut.setText("Gunakan topi dan pakaian tertutup untuk mengurangi paparan sinar UV apabila beraktivitas di luar ruangan");
            }
            else
                {
                    rekomendasiUV.setText("Paparan UV minimal, tidak perlu menggunakan sunscreen");
                    rekomendasiAktivitas.setText("Paparan UV minimal, bebas beraktivitas di luar ruangan");
                    rekomendasiAtribut.setText("Bebas gunakan pakaian apapun tanpa khawatir kulit terbakar");
                }
        }
        else
            {
                rekomendasiUV.setText("Paparan UV ekstrim, wajib menggunakan perlindungan sunscreen minimal SPF-15!");
                rekomendasiAktivitas.setText("Paparan UV ekstrim, hindari aktivitas luar ruangan yang tidak perlu, gunakan sunscreen secara berkala!");
                rekomendasiAtribut.setText("Wajib menggunakan topi, pakaian tertutup, serta kaca mata UV untuk mengurangi paparan sinar UV apabila beraktivitas di luar ruangan");

            }

        if (kodeCuacaCurrent<600) //kondisi di saat ini
        {
            rekomendasiAtribut.setText("Cuaca sedang hujan, gunakan jas hujan, jaket, mantel atau pakaian tertutup lainnya");
        }
        else if (kodeCuacaCurrent>802)
        {

        }

        if (kodeCuacaForecast<600||kodeCuacaForecast>802) //kondisi di masa depan, hujan
        {
            rekomendasiPrediksi.setText("Hari ini kemungkinan akan hujan, persiapkan payung dan pakaian luaran");
        }
        else if (kodeCuacaForecast>799 && kodeCuacaForecast<=802) //kondisi di masa depan, tidak hujan
            {
                if(kodeCuacaForecast==800)
                {
                    rekomendasiPrediksi.setText("Hari ini kemungkinan cerah");
                }
                else
                    {
                        rekomendasiPrediksi.setText("Hari ini cerah berawan, alangkah baiknya membawa payung");
                    }
            }
        else //600 sampe 799  atau cuaca polusi dan angin kencang
            {
                rekomendasiPrediksi.setText("Hari kemungkinan berpolusi atau angin kencang, persiapkan pakaian sesuai");
            }

    }
}
