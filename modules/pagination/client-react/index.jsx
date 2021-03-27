import React from 'react';
import { Route, NavLink } from 'react-router-dom';
import { translate } from '@module/i18n-client-react';
import { MenuItem } from '@module/look-client-react';
import ClientModule from '@module/module-client-react';

import PaginationDemo from './containers/PaginationDemo';
import resources from './locales';

const NavLinkWithI18n = translate()(({ t }) => (
  <NavLink to="/pagination" className="nav-link" activeClassName="active">
    {t('pagination:navLink')}
  </NavLink>
));

export default new ClientModule({
  route: [<Route exact path="/pagination" component={PaginationDemo} />],
  navItem: [
    <MenuItem key="/pagination">
      <NavLinkWithI18n />
    </MenuItem>
  ],
  localization: [{ ns: 'pagination', resources }]
});
