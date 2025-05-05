import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    scenarios: {
        first_endpoint: {
            executor: 'constant-vus',
            exec: 'testEndpoint1',
            vus: 500,
            duration: '30s',
            startTime: '0s',
        },
        second_endpoint: {
            executor: 'constant-vus',
            exec: 'testEndpoint2',
            vus: 500,
            duration: '30s',
            startTime: '31s',
        },
        third_endpoint: {
            executor: 'constant-vus',
            exec: 'testEndpoint3',
            vus: 500,
            duration: '30s',
            startTime: '70s',
        },
    },
};

export function testEndpoint1() {
    const res = http.get('http://host.docker.internal:8080');
    check(res, { 'status is 200': (r) => r.status === 200 });
    sleep(1);
}

export function testEndpoint2() {
    const res = http.get('http://host.docker.internal:8080/benchmark?count=40&durationMs=5&parallelism=1');
    check(res, { 'status is 200': (r) => r.status === 200 });
    sleep(1);
}

export function testEndpoint3() {
    const res = http.get('http://host.docker.internal:8080/benchmark?count=40&durationMs=5&parallelism=4');
    check(res, { 'status is 200': (r) => r.status === 200 });
    sleep(1);
}
