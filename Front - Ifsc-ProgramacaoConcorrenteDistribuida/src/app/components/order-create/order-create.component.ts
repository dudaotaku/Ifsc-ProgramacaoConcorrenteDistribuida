import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService, Produto, Pedido } from '../../services/order.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-order-create',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './order-create.component.html',
  styleUrl: './order-create.component.css'
})
export class OrderCreateComponent implements OnInit {
  // Signals for state tracking (Zoneless Angular 18+ mode)
  readonly produtos = signal<Produto[]>([]);
  readonly selectedProdutos = signal<Set<Produto>>(new Set<Produto>());
  
  customerName = '';
  readonly loadingProdutos = signal(true);
  readonly submitting = signal(false);
  readonly seeding = signal(false);
  
  readonly successMessage = signal('');
  readonly errorMessage = signal('');

  constructor(private readonly orderService: OrderService) {}

  ngOnInit(): void {
    this.loadCatalog();
  }

  loadCatalog(): void {
    this.loadingProdutos.set(true);
    this.orderService.getProdutos().subscribe({
      next: (data) => {
        this.produtos.set(data || []);
        this.loadingProdutos.set(false);
      },
      error: (err) => {
        console.error('Failed to load products catalog:', err);
        this.errorMessage.set('Não foi possível carregar o cardápio. Verifique se o backend está rodando.');
        this.loadingProdutos.set(false);
      }
    });
  }

  toggleProductSelection(produto: Produto): void {
    this.selectedProdutos.update((set) => {
      const nextSet = new Set(set);
      if (nextSet.has(produto)) {
        nextSet.delete(produto);
      } else {
        nextSet.add(produto);
      }
      return nextSet;
    });
  }

  isProductSelected(produto: Produto): boolean {
    return this.selectedProdutos().has(produto);
  }

  // Calculate total price of currently selected items
  getSelectedTotal(): number {
    let total = 0;
    this.selectedProdutos().forEach(p => total += p.preco);
    return total;
  }

  submitOrder(): void {
    // Validations
    if (!this.customerName.trim()) {
      this.errorMessage.set('Por favor, insira o nome do cliente.');
      return;
    }
    
    if (this.selectedProdutos().size === 0) {
      this.errorMessage.set('Selecione pelo menos um item do cardápio.');
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    // Join product names to store in the 'descricao' database field
    const productsListNames = Array.from(this.selectedProdutos())
      .map(p => p.nome_produto)
      .join(', ');

    const newPedido: Pedido = {
      nomeCliente: this.customerName.trim(),
      descricao: productsListNames
    };

    this.orderService.createPedido(newPedido).subscribe({
      next: (created) => {
        console.log('Order created successfully:', created);
        this.successMessage.set(`Pedido #${created.id} para ${created.nomeCliente} enviado com sucesso para a cozinha!`);
        this.resetForm();
        this.submitting.set(false);
      },
      error: (err) => {
        console.error('Failed to submit order:', err);
        this.errorMessage.set('Falha ao criar o pedido. Verifique o servidor e tente novamente.');
        this.submitting.set(false);
      }
    });
  }

  resetForm(): void {
    this.customerName = '';
    this.selectedProdutos.set(new Set<Produto>());
  }

  // Seeding mock products
  seedSampleProducts(): void {
    this.seeding.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    const sampleProducts: Produto[] = [
      {
        nome_produto: 'Cheeseburger Duplo',
        preco: 28.90,
        descricao: 'Pão brioche, duas carnes de 120g grelhadas, cheddar duplo e molho artesanal'
      },
      {
        nome_produto: 'Batata Rústica Especial',
        preco: 15.50,
        descricao: 'Batatas fritas rústicas com casca, temperadas com alecrim, alho e páprica'
      },
      {
        nome_produto: 'Pizza de Calabresa 30cm',
        preco: 45.00,
        descricao: 'Molho de tomate artesanal, mussarela, calabresa defumada fatiada e orégano'
      },
      {
        nome_produto: 'Refrigerante Lata 350ml',
        preco: 6.00,
        descricao: 'Coca-Cola ou Guaraná Antarctica gelados'
      },
      {
        nome_produto: 'Petit Gâteau com Sorvete',
        preco: 19.90,
        descricao: 'Bolo quente de chocolate cremoso com bola de sorvete de creme italiana'
      }
    ];

    const requests = sampleProducts.map(p => this.orderService.saveProduto(p));

    forkJoin(requests).subscribe({
      next: (results) => {
        console.log('Successfully seeded database catalog:', results);
        this.successMessage.set('Cardápio de teste gerado com sucesso! Divirta-se criando pedidos.');
        this.seeding.set(false);
        this.loadCatalog(); // reload
      },
      error: (err) => {
        console.error('Failed to seed catalog database:', err);
        this.errorMessage.set('Falha ao criar o cardápio no backend. Verifique a conexão com o banco.');
        this.seeding.set(false);
      }
    });
  }
}
