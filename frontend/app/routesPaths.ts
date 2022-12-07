import { IconProps } from '@chakra-ui/react';
import { ReactComponentElement, ReactNode } from 'react';

import { HomeIcon } from '../components/Icon/Icons';


export type RoutePath = {
    path: string
    name: string
    icon?: string | React.FC<IconProps>
    category: boolean
    collapse: boolean
    routes?: RoutePath[]
}

const routePaths: RoutePath[] = [
    {
        path: "/",
        name: "Home",
        icon: HomeIcon,
        category: false,
        collapse: true,
        routes: [
            {
                path: "/products",
                name: "Products",
                category: false,
                collapse: false,
            }
        ]
    },
];

export default routePaths;