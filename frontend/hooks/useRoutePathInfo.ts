import { useRouter } from 'next/router';
import { useMemo } from 'react';

import { RoutePath } from './../app/routesPaths';

const useRoutePathInfo = (route: RoutePath) => {
    const router = useRouter();

    const isActive = useMemo(() => router.pathname.includes(route.path), [route.path, router.pathname]);

    return { isActive };
}

export default useRoutePathInfo;