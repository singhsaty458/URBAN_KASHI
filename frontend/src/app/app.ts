import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterOutlet } from '@angular/router';

interface DashboardData {
  todayRevenue: number; todayTransactions: number; monthRevenue: number; monthTransactions: number;
  totalCustomers: number; lowStockCount: number;
  recentInvoices: Array<{ invoiceNumber: string; grandTotal: number; paymentMode: string; createdAt: string }>;
  lowStockItems: Array<{ size?: string; color?: string; stockQuantity: number; product?: { name: string } }>;
}

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit {
  protected readonly title = signal('Urban Kashi');
  protected readonly loading = signal(true);
  protected readonly error = signal('');
  protected readonly dashboard = signal<DashboardData | null>(null);

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<DashboardData>('http://localhost:8080/api/dashboard').subscribe({
      next: (data) => { this.dashboard.set(data); this.loading.set(false); },
      error: () => { this.error.set('Dashboard data load nahi ho paaya. Backend running hai?'); this.loading.set(false); }
    });
  }

  protected formatCurrency(value = 0): string {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(value);
  }

  protected formatDate(value: string): string {
    return new Intl.DateTimeFormat('en-IN', { day: '2-digit', month: 'short', year: 'numeric' }).format(new Date(value));
  }
}
