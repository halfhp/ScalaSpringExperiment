import http from 'k6/http';
import { check, sleep } from 'k6';


export const options = {
    scenarios: {
        lightweight_requests: {
            executor: 'ramping-vus',
            stages: [
                { duration: '30s', target: 50 },
                { duration: '30', target: 50 },
                { duration: '30s', target: 0 },
            ],
            exec: 'lightweight_request',
        },

        moderate_requests: {
            executor: 'ramping-vus',
            stages: [
                { duration: '30s', target: 3 },
                { duration: '30', target: 3 },
                { duration: '30s', target: 0 },
            ],
            exec: 'moderate_cpu_request',
        },

        heavy_requests: {
            executor: 'ramping-vus',
            stages: [
                { duration: '30s', target: 10 },
                { duration: '30', target: 10 },
                { duration: '30s', target: 0 },
            ],
            exec: 'heavy_cpu_request',
        },
    },
};

const baseUri = 'http://host.docker.internal:8080';

export function lightweight_request() {
    const res = http.get(`${baseUri}/benchmark?count=1&durationMs=1&parallelism=1&waitIntervalMs=1`);
    check(res, { 'status is 200': (r) => r.status === 200 });
    sleep(1);
}

export function moderate_cpu_request() {
    const res = http.get(`${baseUri}/benchmark?count=20&durationMs=5&parallelism=2&waitIntervalMs=10`);
    check(res, { 'status is 200': (r) => r.status === 200 });
    sleep(1);
}

export function heavy_cpu_request() {
    const res = http.get(`${baseUri}/benchmark?count=40&durationMs=10&parallelism=2&waitIntervalMs=10`);
    check(res, { 'status is 200': (r) => r.status === 200 });
    sleep(1);
}
