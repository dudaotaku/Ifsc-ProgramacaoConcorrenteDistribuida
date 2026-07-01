import { Routes } from '@angular/router';
import { KitchenKanbanComponent } from './components/kitchen-kanban/kitchen-kanban.component';
import { OrderCreateComponent } from './components/order-create/order-create.component';

export const routes: Routes = [
  {
    path: '',
    component: KitchenKanbanComponent
  },
  {
    path: 'cozinha',
    component: KitchenKanbanComponent
  },
  {
    path: 'fazer-pedido',
    component: OrderCreateComponent
  },
  {
    path: '**',
    redirectTo: '',
    pathMatch: 'full'
  }
];
