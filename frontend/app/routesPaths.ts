import { IconProps } from '@chakra-ui/react';
import { FaWrench, FaBox } from "react-icons/fa";

import { HomeIcon } from '../components/Icon/Icons';


export type RoutePath = {
    path: string
    name: string
    icon?: string | React.FC<IconProps> | any
    category?: boolean
    collapse?: boolean
    routes?: RoutePath[]
}

const routePaths: RoutePath[] = [
    {
        path: "/dashboard",
        name: "Dashboard",
        icon: HomeIcon,
    },
    {
        path: "/maintenance",
        name: "MANUTENÇÃO",
        icon: FaWrench,
        category: true,
        routes: [
            {
                path: "/maintenance/products",
                name: "Produtos",
                icon: FaBox,
                collapse: true,
                routes: [
                    {
                        path: '/maintenance/products',
                        name: 'Cadastro',
                    },
                    {
                        path: '/maintenance/products/groups',
                        name: 'Grupos'
                    },
                    {
                        path: '/maintenance/products/types',
                        name: 'Tipos'
                    },
                ]
            }
        ]
    },
];

export default routePaths;