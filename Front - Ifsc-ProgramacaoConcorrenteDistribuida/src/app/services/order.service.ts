import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Produto {
  id?: number;
  nome_produto: string;
  preco: number;
  descricao: string;
}

export interface Pedido {
  id?: number;
  nomeCliente: string;
  descricao: string; // Storing selected products formatted list (e.g. "Cheeseburger, Fries")
  status?: string;   // RECEBIDO, AGUARDANDO, EM_PREPARO, PRONTO, ENTREGUE
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly baseUrl = 'http://localhost:8080';

  constructor(private readonly http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json'
    });
  }

  // Maps GET /pedidos -> GET /pedido/listar
  getPedidos(): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(`${this.baseUrl}/pedido/listar`);
  }

  // Maps POST /pedidos -> POST /pedido
  createPedido(pedido: Pedido): Observable<Pedido> {
    return this.http.post<Pedido>(`${this.baseUrl}/pedido`, pedido, {
      headers: this.getHeaders()
    });
  }

  // Maps GET /produtos -> GET /produto/listar
  getProdutos(): Observable<Produto[]> {
    return this.http.get<Produto[]>(`${this.baseUrl}/produto/listar`);
  }

  // Maps POST /produto (for seeding)
  saveProduto(produto: Produto): Observable<Produto> {
    return this.http.post<Produto>(`${this.baseUrl}/produto`, produto, {
      headers: this.getHeaders()
    });
  }

  // Put status transitions mapping to REST API:
  // AGUARDANDO -> EM_PREPARO
  iniciarPedido(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/pedido/${id}/iniciar`, {}, {
      headers: this.getHeaders()
    });
  }

  // EM_PREPARO -> PRONTO
  finalizarPedido(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/pedido/${id}/finalizar`, {}, {
      headers: this.getHeaders()
    });
  }

  // PRONTO -> ENTREGUE
  entregarPedido(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/pedido/${id}/entregar`, {}, {
      headers: this.getHeaders()
    });
  }
}
