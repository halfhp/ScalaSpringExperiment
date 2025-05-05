import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 500,
    duration: '30s',
};

export default function () {
    const res = http.get('http://host.docker.internal:8080');
    check(res, { 'status is 200': (r) => r.status === 200 });
    sleep(1);
}
