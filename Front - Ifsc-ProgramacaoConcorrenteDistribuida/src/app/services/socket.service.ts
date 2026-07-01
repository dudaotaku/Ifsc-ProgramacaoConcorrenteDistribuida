import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import io from 'socket.io-client';
import { Pedido } from './order.service';

@Injectable({
  providedIn: 'root'
})
export class SocketService implements OnDestroy {
  private socket!: SocketIOClient.Socket;
  private readonly socketUrl = 'http://localhost:9092';
  
  private readonly connected$ = new BehaviorSubject<boolean>(false);
  private readonly novoPedido$ = new Subject<Pedido>();
  private readonly atualizacaoPedido$ = new Subject<Pedido>();
  private readonly pedidoEntregue$ = new Subject<Pedido>();

  constructor() {
    this.connect();
  }

  private connect(): void {
    console.log('[SocketService] Connecting to Socket.IO server at', this.socketUrl);
    
    // Establishing socket.io connection with reconnection enabled by default
    this.socket = io(this.socketUrl, {
      reconnection: true,
      reconnectionAttempts: Infinity,
      reconnectionDelay: 1000,
      reconnectionDelayMax: 5000,
      timeout: 20000,
      transports: ['websocket', 'polling']
    });

    this.socket.on('connect', () => {
      console.log('[SocketService] Connected successfully');
      this.connected$.next(true);
    });

    this.socket.on('disconnect', (reason: string) => {
      console.warn('[SocketService] Disconnected:', reason);
      this.connected$.next(false);
    });

    this.socket.on('connect_error', (error: any) => {
      console.error('[SocketService] Connection error:', error);
      this.connected$.next(false);
    });

    // Listen to real-time events from server
    // Trailing colon and space are part of the event name for "novoPedido: "
    this.socket.on('novoPedido: ', (pedido: Pedido) => {
      console.log('[SocketService] Event "novoPedido: " received:', pedido);
      this.novoPedido$.next(pedido);
    });

    this.socket.on('atualizacaoPedido', (pedido: Pedido) => {
      console.log('[SocketService] Event "atualizacaoPedido" received:', pedido);
      this.atualizacaoPedido$.next(pedido);
    });

    this.socket.on('pedidoEntregue', (pedido: Pedido) => {
      console.log('[SocketService] Event "pedidoEntregue" received:', pedido);
      this.pedidoEntregue$.next(pedido);
    });
  }

  // Getters for status and events
  get isConnected$(): Observable<boolean> {
    return this.connected$.asObservable();
  }

  get onNovoPedido$(): Observable<Pedido> {
    return this.novoPedido$.asObservable();
  }

  get onAtualizacaoPedido$(): Observable<Pedido> {
    return this.atualizacaoPedido$.asObservable();
  }

  get onPedidoEntregue$(): Observable<Pedido> {
    return this.pedidoEntregue$.asObservable();
  }

  ngOnDestroy(): void {
    if (this.socket) {
      console.log('[SocketService] Disconnecting socket');
      this.socket.disconnect();
    }
  }
}
