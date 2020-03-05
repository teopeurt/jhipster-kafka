import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'store',
        loadChildren: () => import('./store/store/store.module').then(m => m.StoreStoreModule)
      },
      {
        path: 'store-alert',
        loadChildren: () => import('./alert/store-alert/store-alert.module').then(m => m.AlertStoreAlertModule)
      }
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ])
  ]
})
export class GatewayEntityModule {}
