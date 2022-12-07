import React, { Fragment, ReactNode } from 'react';

import { RoutePath } from '../../../app/routesPaths';
import SidebarLinkCategory from './SidebarLinkCategory';
import SidebarLinkCollapse from './SidebarLinkCollapse';
import SidebarLinkText from './SidebarLinkText';

export type SidebarLinkProps = {
    route: RoutePath
};

const SidebarLink: React.FC<SidebarLinkProps> = ({ route }) => {
    return (
        <Fragment>
            {!!route.category && (<SidebarLinkCategory route={route} />)}
            {!!route.collapse && (<SidebarLinkCollapse route={route} />)}
            {!route.category && !route.collapse && (<SidebarLinkText route={route} />)}
        </Fragment>
    );
}

export default SidebarLink;