package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

@Composable
fun ManualBookDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSection by remember { mutableStateOf(0) } // 0: Pendahuluan, 1: Antarmuka, 2: Lapangan, 3: Administrasi

    // Simple styling specs for manual dialog
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = SharpShapes.medium,
            color = BlackPure,
            border = BorderStroke(1.dp, BorderGrey)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar of Manual inside the app
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkGrey)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = LimeNeon,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "BUKU PANDUAN PENGGUNA",
                                color = LimeNeon,
                                style = MaterialTheme.typography.labelSmall,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "SI-RAYYAN Logistik & Keuangan",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // PDF Print Button
                        Button(
                            onClick = {
                                val html = generateManualBookHtml()
                                PdfPrintUtil.printHtml(context, html, "ManualBook_SI_RAYYAN")
                                Toast.makeText(context, "Mencetak Buku Panduan...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                            shape = SharpShapes.small,
                            modifier = Modifier.testTag("print_manual_pdf_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CETAK PDF",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        // Close Button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.border(1.dp, BorderGrey, SharpShapes.small)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Nav / Scroll tabs for segments
                ScrollableTabRow(
                    selectedTabIndex = selectedSection,
                    containerColor = MediumGrey,
                    contentColor = LimeNeon,
                    edgePadding = 12.dp
                ) {
                    Tab(
                        selected = selectedSection == 0,
                        onClick = { selectedSection = 0 },
                        text = { Text("1. Pendahuluan", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedSection == 1,
                        onClick = { selectedSection = 1 },
                        text = { Text("2. Antarmuka", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedSection == 2,
                        onClick = { selectedSection = 2 },
                        text = { Text("3. Tim Lapangan", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedSection == 3,
                        onClick = { selectedSection = 3 },
                        text = { Text("4. Administrasi", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                // Core content area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(BlackPure)
                        .padding(20.dp)
                ) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (selectedSection) {
                            0 -> IntroductionSection()
                            1 -> UIOverviewSection()
                            2 -> FieldGuideSection()
                            3 -> AdminGuideSection()
                        }
                    }
                }

                // Call to action info panel at footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkGrey)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = LimeNeon,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tips: Tekan tombol CETAK PDF di atas untuk langsung menyimpan manual book ke memori ponsel.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun IntroductionSection() {
    Text(
        text = "1. PENDAHULUAN & FUNGSI UTAMA",
        style = MaterialTheme.typography.titleLarge,
        color = LimeNeon,
        fontWeight = FontWeight.Black
    )
    HorizontalDivider(color = BorderGrey)
    
    Text(
        text = "Sistem Informasi Manajemen Pengapalan dan Administrasi Penagihan Konstruksi " +
               "PT. RAYYAN KARYA (SI-RAYYAN) dirancang khusus untuk menjembatani kesenjangan koordinasi data " +
               "antara Tim Pengawas di Lapangan (Lokasi Proyek) dengan Tim Keuangan & Administrasi di Kantor Pusat.",
        color = Color.White,
        lineHeight = 20.sp,
        fontSize = 14.sp
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkGrey),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderGrey, SharpShapes.small)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "FUNGSI UTAMA APLIKASI:",
                color = LimeNeon,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
            BulletItem("Pencatatan Surat Jalan Lapangan", "Merekam langsung volume kiriman material konstruksi baja ringan, aluminium, spandek secara realtime di tapak kerja.")
            BulletItem("Validasi Lampiran Foto Fisik", "Memastikan bukti pembongkaran muatan terlampir guna menghindari fraud data logistik lapangan.")
            BulletItem("Verifikasi Alur Logistik", "Menyediakan persetujuan logistik (TERIMA) oleh penanggungjawab lapangan sehingga sisa kuota penagihan terupdate instan.")
            BulletItem("Creator Invoice Penagihan Dinamis", "Menyusun faktur administrasi tanpa keterikatan nilai kaku. Persentase pembayaran Down Payment (DP) dan tarif pajak PPN dapat disesuaikan bebas sesuai kesepakatan PO.")
            BulletItem("Pencetakan PDF Kertas A4 Standar", "Mengekspor draft invoice dan dokumen buku panduan ini ke dalam format PDF standar resmi yang siap dikirim langsung ke klien.")
        }
    }
}

@Composable
fun UIOverviewSection() {
    Text(
        text = "2. PENJELASAN TATA LETAK ANTARMUKA",
        style = MaterialTheme.typography.titleLarge,
        color = LimeNeon,
        fontWeight = FontWeight.Black
    )
    HorizontalDivider(color = BorderGrey)

    Text(
        text = "Aplikasi SI-RAYYAN dirancang dengan arsitektur navigasi bawah (Bottom Navigation) tunggal untuk kemudahan rotasi tugas. Berikut perincian fungsionalitas visual utama:",
        color = Color.White,
        lineHeight = 20.sp,
        fontSize = 14.sp
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InterfaceCard(
            title = "A. Dasbor Utama (Dashboard)",
            description = "Pusat statistik eksekutif menampilkan total proyek konstruksi aktif, indikator pengiriman tertunda, grafik batang rupiah perputaran aktivitas material per hari, serta total sisa dana penagihan yang masih menggantung. Dasbor ini membantu pemilik mengamati kondisi keuangan dalam sekali lirik."
        )
        InterfaceCard(
            title = "B. Modul Logistik (Logistics)",
            description = "Area operasional lapangan tempat surat jalan baru dibuat dan divalidasi. Menampilkan daftar riwayat surat jalan, nomor mobil logistik, nama driver, serta status material lengkap dengan tombol TERIMA logistik masuk."
        )
        InterfaceCard(
            title = "C. Modul Penagihan (Billing)",
            description = "Tab keuangan yang mengawasi proyeksi keuangan per situs proyek konstruksi. Terdapat fungsi mencatatkan transfer pembayaran termin klien baru (INPUT BAYAR) dan tombol untuk merancang Invoice digital."
        )
    }
}

@Composable
fun FieldGuideSection() {
    Text(
        text = "3. PANDUAN LANGKAH TIM LAPANGAN (LOGISTIK)",
        style = MaterialTheme.typography.titleLarge,
        color = LimeNeon,
        fontWeight = FontWeight.Black
    )
    HorizontalDivider(color = BorderGrey)

    Text(
        text = "Ikuti langkah berikut untuk menginput pengiriman material konstruksi dari gudang supplier menuju tapak proyek:",
        color = Color.White,
        fontSize = 14.sp
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        StepItem(
            step = "1",
            title = "Buka Modul Logistik & Klik Buat Surat Jalan",
            desc = "Akses tab 'Logistik' dari navigasi bawah, lalu klik tombol hijau menyala bertuliskan 'BUAT SURAT JALAN' di bagian atas layar."
        )
        StepItem(
            step = "2",
            title = "Isi Form Surat Jalan Lapangan",
            desc = "Isilah formulir dengan teliti:\n• Pilih Proyek tujuan pengiriman.\n• Ketik Plat Nomor Kendaraan logistik (misal: B 9243 KFB).\n• Ketik Nama Sopir ekspedisi.\n• Pilih/Ketik Jenis Material yang dimuat (misal: Reng Baja Ringan, Spandek C-75).\n• Ketik Jumlah Volume material (Angka) beserta Satuannya (Batang/Pcs/M2)."
        )
        StepItem(
            step = "3",
            title = "Lampirkan Gambar Foto Bukti Bongkar",
            desc = "Tekan tombol 'LAMPIRAKAN FOTO FISIK BONGKAR MUAT'. Pilih file foto kendaraan ekspedisi beserta muatan yang telah tiba sukses di lokasi bongkar muat lapangan demi keamanan validasi."
        )
        StepItem(
            step = "4",
            title = "Kirim / Simpan Draft Logistik",
            desc = "Setelah semua data sesuai, klik tombol 'KIRIM DAN DAFTARKAN'. Pengiriman Anda akan direkam dalam status pending (TERTUNDA) oleh sistem."
        )
        StepItem(
            step = "5",
            title = "Konfirmasi Penerimaan Lapangan oleh Pengawas",
            desc = "Pengawas di lapangan yang menyambut armada truk dapat memantau notifikasi di 'Dasbor Utama' dengan melihat menu 'Konfirmasi Pengiriman Masuk'. Klik tombol 'TERIMA' pada item pengiriman tersebut untuk memastikan barang sukses terverivikasi di lapangan, merubah statusnya menjadi sukses (SUKSES) dan otomatis merubah perhitungan keuangan."
        )
    }
}

@Composable
fun AdminGuideSection() {
    Text(
        text = "4. PANDUAN LANGKAH STAF ADMINISTRASI & KEUANGAN",
        style = MaterialTheme.typography.titleLarge,
        color = LimeNeon,
        fontWeight = FontWeight.Black
    )
    HorizontalDivider(color = BorderGrey)

    Text(
        text = "Ikuti instruksi di bawah ini untuk membuat tagihan (Invoice) resmi Down Payment (DP) berisikan detail dinamis serta pajak PPN:",
        color = Color.White,
        fontSize = 14.sp
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        StepItem(
            step = "1",
            title = "Pergi ke Modul Penagihan",
            desc = "Klik menu navigasi 'Penagihan' di kanan bawah, lalu cari nama proyek proyek konstruksi yang akan diproses. Klik tombol biru 'PENCATATAN / BUAT INVOICE'."
        )
        StepItem(
            step = "2",
            title = "Buka Creator Invoice",
            desc = "Klik tombol 'BUAT DOKUMEN INVOICE BARU'. Anda akan dialihkan ke layar perancang invoice dengan layout split modern."
        )
        StepItem(
            step = "3",
            title = "Entri Data Metadata Invoice",
            desc = "Di panel sebelah kiri, isi rincian metadata dokumen:\n• No. Invoice (bebas diubah misal: 101/RK/VI/2026)\n• No. PO Pembelian\n• Penerima Dokumen (Kepada Yth)\n• Lokasi Detil Pengiriman\n• Persentase Down Payment (DP) - Misal ketik 50% untuk tagihan termin awal.\n• Persentase Pajak PPN - Bebas diubah sesuai aturan (misal ketik 12%)."
        )
        StepItem(
            step = "4",
            title = "Kelola Rincian RABA & Material Dinamis",
            desc = "Tekan tombol tambah (+) di bagian 'RABA & MATERIAL' untuk memasukkan baris barang secara bebas. Anda bisa langsung mengedit Nama Barang, Qty, Satuan, dan Harga SATUAN di list secara interaktif."
        )
        StepItem(
            step = "5",
            title = "Tinjau Live Preview Kertas PDF Resmi",
            desc = "Di sisi kanan layar, saksikan pratinjau lembar kertas putih A0-A4 yang langsung memutakhirkan kalkulasi:\n• Total Nilai Kontrak Dasar (Semua Qty x Harga Barang).\n• Total Nilai DP sesuai input persen (DP 50%).\n• Total PPN sesuai persentase pilihan (PPN 12%).\n• Nilai Tagihan DP Bersih (TOTAL TAGIHAN DP yang HARUS DIBAYAR).\n• Ejaan bahasa Indonesia Terbilang otomatis untuk memudahkan pembayaran."
        )
        StepItem(
            step = "6",
            title = "Cetak Dokumen ke Format PDF Resmi",
            desc = "Klik tombol biru 'CETAK PDF' di pojok kanan atas. Dialog pencetakan Android bawaan akan langsung terbuka, memberikan Anda opsi simpan sebagai PDF langsung ke direktori penyimpanan lokal."
        )
        StepItem(
            step = "7",
            title = "Simpan Data ke Keuangan Utama",
            desc = "Klik tombol 'KIRIM & SIMPAN' untuk mereset dan mengintegrasikan invoice ini kedalam sistem database keuangan, mencatat status termin piutang yang harus dibayar."
        )
    }
}

// Visual helpers
@Composable
fun BulletItem(title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = "• ", color = LimeNeon, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Column {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = desc, color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
fun InterfaceCard(title: String, description: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MediumGrey),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderGrey, SharpShapes.small)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
fun StepItem(step: String, title: String, desc: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(LimeNeon, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = step, color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = desc, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

// Clean HTML template for physical user manual rendering which fully covers client queries in professional formatting style
fun generateManualBookHtml(): String {
    return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="utf-8">
        <title>SI-RAYYAN - Buku Panduan Pengguna</title>
        <style>
            body {
                font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
                color: #000;
                margin: 40px;
                line-height: 1.5;
                font-size: 11px;
            }
            .header-table {
                width: 100%;
                border-collapse: collapse;
                margin-bottom: 5px;
            }
            .header-left {
                width: 75%;
                vertical-align: top;
            }
            .header-right {
                width: 25%;
                text-align: right;
                vertical-align: top;
            }
            .company-name {
                color: #1C3A5E;
                font-size: 21px;
                font-weight: 950;
                margin: 0;
                letter-spacing: 0.5px;
            }
            .company-desc {
                font-size: 10px;
                font-weight: bold;
                margin: 3px 0;
            }
            .divider-double {
                border-top: 4px solid #1C3A5E;
                border-bottom: 2px solid #000;
                height: 4px;
                margin: 8px 0 20px 0;
            }
            .doc-title {
                text-align: center;
                font-size: 20px;
                font-weight: 900;
                letter-spacing: 2px;
                text-decoration: underline;
                margin-bottom: 30px;
                text-transform: uppercase;
                color: #1C3A5E;
            }
            .section-title {
                font-size: 13px;
                font-weight: 900;
                color: #1C3A5E;
                background-color: #f0f4f8;
                padding: 6px 10px;
                margin-top: 25px;
                margin-bottom: 10px;
                border-left: 5px solid #1C3A5E;
                text-transform: uppercase;
            }
            p {
                margin: 0 0 10px 0;
                text-align: justify;
                font-size: 11px;
            }
            ul {
                margin: 0 0 15px 0;
                padding-left: 20px;
            }
            li {
                margin-bottom: 6px;
            }
            .step-container {
                margin-bottom: 15px;
                border-bottom: 1px solid #efefef;
                padding-bottom: 10px;
            }
            .step-header {
                font-weight: bold;
                font-size: 11px;
                color: #000;
                margin-bottom: 4px;
            }
            .step-badge {
                background-color: #1C3A5E;
                color: #fff;
                padding: 1px 6px;
                font-size: 9px;
                font-weight: bold;
                margin-right: 5px;
                border-radius: 3px;
                display: inline-block;
            }
            .step-body {
                font-size: 11px;
                color: #333;
                padding-left: 15px;
            }
            .footer {
                text-align: center;
                margin-top: 50px;
                font-size: 9px;
                color: #777;
                border-top: 1px solid #ddd;
                padding-top: 10px;
            }
        </style>
    </head>
    <body>
        <table class="header-table">
            <tr>
                <td class="header-left">
                    <div class="company-name">PT. RAYYAN KARYA</div>
                    <div class="company-desc">Supplier Atap Baja Ringan, Aluminium, dan Material Konstruksi</div>
                </td>
                <td class="header-right">
                    <div style="font-size: 16px; font-weight: bold; color: #1C3A5E;">SI-RAYYAN</div>
                </td>
            </tr>
        </table>

        <div class="divider-double"></div>

        <div class="doc-title">BUKU PANDUAN MANUAL APLIKASI<br><span style="font-size: 12px; font-weight: normal; letter-spacing: 1px;">Operasional Logistik & Creator Invoice Resmi</span></div>

        <div class="section-title">1. Pendahuluan & Fungsi Utama Aplikasi</div>
        <p>
            Sistem Informasi Manajemen Pengapalan dan Administrasi Penagihan Konstruksi <strong>PT. RAYYAN KARYA (SI-RAYYAN)</strong> dirancang khusus sebagai solusi hibrida digital yang menjembatani operasional Tim Pengawas Logistik Lapangan di tapak konstruksi dengan Staf Keuangan & Administrasi di Kantor Utama.
        </p>
        <p>Aplikasi ini memiliki 5 prioritas fungsi utama:</p>
        <ul>
            <li><strong>Pemberitahuan Surat Jalan Real-Time:</strong> Mencegah salah komunikasi volume kedatangan material baja ringan antara tapak proyek dengan kantor keuangan.</li>
            <li><strong>Anti-Fraud Validasi Gambar:</strong> Memungkinkan sopir ekspedisi mengunggah foto lampir bukti fisik proses bongkar muat secara langsung melalui kamera ponsel.</li>
            <li><strong>Persetujuan Otomatis:</strong> Di dasbor pengawasan, supervisor lapangan cukup sekali klik persetujuan untuk mengubah status logistik menjadi sukses, memperbaharui buku besar sisa piutang klien.</li>
            <li><strong>Pembuat Invoice Fleksibel / Dinamis:</strong> Staf administrasi dapat menentukan persentase Down Payment (DP) termin, tarif pajak PPN sesuai aturan purchase order, dan meraba rincian barang secara fleksibel demi melayani variasi kontrak.</li>
            <li><strong>Cetak PDF Kertas A4 Standar Terpadu:</strong> Menyediakan ekspor dokumen resmi (Invoice & Buku Panduan ini) langsung ke printer nirkabel atau disimpan sebagai berkas PDF instan.</li>
        </ul>

        <div class="section-title">2. Penjelasan Tata Letak Antarmuka (Aplikasi Dashboard)</div>
        <p>
            SI-RAYYAN berjalan di atas sistem navigasi bawah (Bottom Navigation) yang terbagi menjadi tiga pusat kendali utama:
        </p>
        <ul>
            <li><strong>Dasbor Utama (Dashboard):</strong> Berisi ringkasan eksekutif ekat-waktu, meliputi jumlah proyek aktif, jumlah pengiriman pending yang membutuhkan penyambutan lapangan, grafik intensitas pengiriman mingguan (dalam format rupiah), serta sisa volume piutang belum ditagih.</li>
            <li><strong>Layar Logistik (Logistics):</strong> Area khusus ekspedisi yang berisi pelacak riwayat surat jalan lengkap beserta plat mobil, nama supir, volume, jenis material, serta tombol verifikasi 'TERIMA' barang di tapak proyek.</li>
            <li><strong>Layar Penagihan (Billing):</strong> Area akuntan yang merinci status keuangan proyek konstruksi. Memuat fungsi input record kuintansi pembayaran termin klan secara langsung serta tombol akses ke Creator Invoice dinamis.</li>
        </ul>

        <div class="section-title">3. Panduan Langkah-demi-Langkah Untuk Tim Lapangan (Logistik)</div>
        <div class="step-container">
            <div class="step-header"><span class="step-badge">LANGKAH 01</span> Masuk Menu Logistik & Buat Surat Jalan</div>
            <div class="step-body">Ketuk tombol menu 'Logistik' di navigasi bawah ponsel, selanjutnya ketuk tombol hijau 'BUAT SURAT JALAN' di bagian atas halaman logistik.</div>
        </div>
        <div class="step-container">
            <div class="step-header"><span class="step-badge">LANGKAH 02</span> Lengkapi Formulir Surat Jalan</div>
            <div class="step-body">Pilih nama proyek tujuan pengapalan. Ketik rincian armada pengiriman secara valid meliputi silinder plat kendaraan, nama driver, jenis muatan material baja ringan, serta jumlah kuantitas volume barang dan satuannya.</div>
        </div>
        <div class="step-container">
            <div class="step-header"><span class="step-badge">LANGKAH 03</span> Ambil Foto Fisik Bongkar Muat</div>
            <div class="step-body">Ketuk tombol berlabel kamera di bawah form untuk memicu file picker, unggah berkas foto kondisi truk/material di lapangan saat proses drop barang sebagai pengesahan drop fisik. Ketuk 'KIRIM DAN DAFTARKAN'.</div>
        </div>
        <div class="step-container">
            <div class="step-header"><span class="step-badge">LANGKAH 04</span> Verivikasi Pengiriman untuk Keuangan</div>
            <div class="step-body">Ketika sopir tiba di lokasi, pengawas lapangan wajib membuka 'Dasbor Utama' aplikasi. Pada deret menu 'Konfirmasi Pengiriman Masuk', teliti rinciannya lalu klik tombol 'TERIMA' untuk menyetujui logistik sukses.</div>
        </div>

        <div class="section-title">4. Panduan Langkah-demi-Langkah Untuk Staf Administrasi & Kantor</div>
        <div class="step-container">
            <div class="step-header"><span class="step-badge">LANGKAH 01</span> Buka Modul Penagihan & Pilih Proyek</div>
            <div class="step-body">Ketuk menu 'Penagihan' di navigasi bawah, temukan proyek konstruksi proyek yang ingin ditarik terminnya, lalu ketuk tombol biru 'PENCATATAN / BUAT INVOICE'.</div>
        </div>
        <div class="step-container">
            <div class="step-header"><span class="step-badge">LANGKAH 02</span> Luncurkan Creator Perancang Invoice</div>
            <div class="step-body">Ketuk tombol bergaris emas 'BUAT DOKUMEN INVOICE BARU' untuk meluncurkan antarmuka perancang invoice terbelah (Split Layout).</div>
        </div>
        <div class="step-container">
            <div class="step-header"><span class="step-badge">LANGKAH 03</span> Masukkan Metadata Invoice Dinamis Berubah</div>
            <div class="step-body">Di panel entri kiri, Anda dapat dengan bebas mengetikkan nomor faktur (invoice nomor), tanggal cetak, nomor PO (Purchase Order), Kepada Yth penerima, serta alamat destinasi lokasi pengiriman.</div>
        </div>
        <div class="step-container">
            <div class="step-header"><span class="step-badge">LANGKAH 04</span> Kelola Rincian RABA & Material Bebas</div>
            <div class="step-body">Ketik persentase DP yang diinginkan (contoh: 50%) dan tarif pajak PPN (contoh: 12%). Pada tabel bawahnya, kreasikan item barang dengan mengetikkan deskripsi besi baja, masukan Qty, satuan unit, dan harga per unit di list edit instan secara adaptif.</div>
        </div>
        <div class="step-container">
            <div class="step-header"><span class="step-badge">LANGKAH 05</span> Tinjau Pratinjau Kertas Resmi di Sisi Kanan</div>
            <div class="step-body">Perhatikan panel kanan yang menyajikan duplikasi kertas putih resmi. Angka secara dinamis menghitung Total Kontrak Dasar, Nilai Tagihan DP sesuai persentase, Beban Pajak PPN (12%), total tagihan DP bersih yang harus dibayar, lengkap beserta ejaan teks Terbilang Indonesia otomatis secara presisi.</div>
        </div>
        <div class="step-container">
            <div class="step-header"><span class="step-badge">LANGKAH 06</span> Pencetakan & Penyimpanan Resmi</div>
            <div class="step-body">Ketuk tombol biru 'CETAK PDF' di pojok kanan atas untuk memanggil print dialog bawaan Android. Anda langsung dapat mencetaknya fisik atau mengekspornya menjadi format file PDF resmi. Klik tombol 'KIRIM & SIMPAN' untuk mengintegrasikan rekap piutang tersebut kedalam database keuangan utama.</div>
        </div>

        <div class="footer">
            Buku Panduan Operasional SI-RAYYAN &copy; 2026 PT. RAYYAN KARYA. Seluruh hak cipta dilindungi undang-undang.<br>
            Dokumen ini di-generate secara otomatis via Sistem Cetak PDF SI-RAYYAN.
        </div>
    </body>
    </html>
    """.trimIndent()
}
