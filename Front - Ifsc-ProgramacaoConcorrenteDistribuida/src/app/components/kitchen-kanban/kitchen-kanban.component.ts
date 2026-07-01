import { Component, OnInit, OnDestroy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderService, Pedido } from '../../services/order.service';
import { SocketService } from '../../services/socket.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-kitchen-kanban',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './kitchen-kanban.component.html',
  styleUrl: './kitchen-kanban.component.css'
})
export class KitchenKanbanComponent implements OnInit, OnDestroy {
  // Signals for state tracking (critical for Zoneless Angular 18+ mode)
  readonly pedidos = signal<Pedido[]>([]);
  readonly socketConnected = signal(false);
  readonly errorMessage = signal('');
  readonly loading = signal(true);

  // Computed columns list, auto-recalculates when pedidos() signal changes
  readonly columns = computed(() => {
    const cols: { [key: string]: Pedido[] } = {
      'RECEBIDO': [],
      'AGUARDANDO': [],
      'EM_PREPARO': [],
      'PRONTO': [],
      'ENTREGUE': []
    };
    
    this.pedidos().forEach((pedido) => {
      const status = pedido.status || 'RECEBIDO';
      if (cols[status]) {
        cols[status].push(pedido);
      } else {
        cols['RECEBIDO'].push(pedido);
      }
    });

    return cols;
  });

  readonly statusList = ['RECEBIDO', 'AGUARDANDO', 'EM_PREPARO', 'PRONTO', 'ENTREGUE'];
  private readonly subscriptions = new Subscription();

  constructor(
    private readonly orderService: OrderService,
    private readonly socketService: SocketService
  ) {}

  ngOnInit(): void {
    this.loadInitialPedidos();
    this.setupSocketListeners();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  loadInitialPedidos(): void {
    this.loading.set(true);
    this.orderService.getPedidos().subscribe({
      next: (data) => {
        this.pedidos.set(data || []);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Failed to load initial orders:', err);
        this.errorMessage.set('Não foi possível carregar os pedidos. Verifique se o servidor backend está rodando.');
        this.loading.set(false);
      }
    });
  }

  setupSocketListeners(): void {
    // Connection Status
    this.subscriptions.add(
      this.socketService.isConnected$.subscribe((connected) => {
        this.socketConnected.set(connected);
      })
    );

    // New Order Event (RECEBIDO -> AGUARDANDO via worker)
    this.subscriptions.add(
      this.socketService.onNovoPedido$.subscribe((pedido) => {
        this.addOrUpdatePedido(pedido);
      })
    );

    // Update Order Event
    this.subscriptions.add(
      this.socketService.onAtualizacaoPedido$.subscribe((pedido) => {
        this.addOrUpdatePedido(pedido);
      })
    );

    // Delivered Order Event
    this.subscriptions.add(
      this.socketService.onPedidoEntregue$.subscribe((pedido) => {
        this.addOrUpdatePedido(pedido);
      })
    );
  }

  private addOrUpdatePedido(pedido: Pedido): void {
    if (!pedido || !pedido.id) return;
    
    const current = this.pedidos();
    const index = current.findIndex(p => p.id === pedido.id);
    
    if (index > -1) {
      const updated = [...current];
      updated[index] = { ...updated[index], ...pedido };
      this.pedidos.set(updated);
    } else {
      this.pedidos.set([...current, pedido]);
    }
  }

  // Get Column Label/Translate status
  getColumnLabel(status: string): string {
    switch (status) {
      case 'RECEBIDO': return 'Recebidos';
      case 'AGUARDANDO': return 'Aguardando';
      case 'EM_PREPARO': return 'Em Preparo';
      case 'PRONTO': return 'Prontos';
      case 'ENTREGUE': return 'Entregues';
      default: return status;
    }
  }

  // Get button text to advance status
  getAdvanceButtonText(status: string): string {
    switch (status) {
      case 'RECEBIDO': return 'Aguardar';
      case 'AGUARDANDO': return 'Iniciar Preparo';
      case 'EM_PREPARO': return 'Finalizar Preparo';
      case 'PRONTO': return 'Entregar Pedido';
      default: return 'Avançar';
    }
  }

  advanceStatus(pedido: Pedido): void {
    if (!pedido.id) return;
    
    const currentStatus = pedido.status;
    let transition$;

    if (currentStatus === 'AGUARDANDO') {
      transition$ = this.orderService.iniciarPedido(pedido.id);
    } else if (currentStatus === 'EM_PREPARO') {
      transition$ = this.orderService.finalizarPedido(pedido.id);
    } else if (currentStatus === 'PRONTO') {
      transition$ = this.orderService.entregarPedido(pedido.id);
    } else if (currentStatus === 'RECEBIDO') {
      console.log('Order is still in RECEBIDO status. Waiting for backend queue.');
      return;
    } else {
      return; // ENTREGUE is terminal
    }

    transition$.subscribe({
      next: (response: any) => {
        if (response && response.status) {
          // Update local state immediately
          const current = this.pedidos();
          const index = current.findIndex(p => p.id === pedido.id);
          if (index > -1) {
            const updated = [...current];
            updated[index] = { ...updated[index], status: response.status };
            this.pedidos.set(updated);
          }
        }
      },
      error: (err) => {
        console.error('Failed to advance order status:', err);
        this.errorMessage.set('Não foi possível atualizar o status do pedido.');
        setTimeout(() => this.errorMessage.set(''), 4000);
      }
    });
  }
}
