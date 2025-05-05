import http from 'k6/http';
import { check, sleep } from 'k6';


export const options = {
    scenarios: {
        lightweight_requests: {
            executor: 'ramping-vus',
            stages: [
                { duration: '30s', target: 75 },
                { duration: '30', target: 75 },
                { duration: '30s', target: 0 },
            ],
            exec: 'lightweight_request',
        },

        moderate_requests: {
            executor: 'ramping-vus',
            stages: [
                { duration: '30s', target: 25 },
                { duration: '30', target: 25 },
                { duration: '30s', target: 0 },
            ],
            exec: 'moderate_request',
        },

        heavy_requests: {
            executor: 'ramping-vus',
            stages: [
                { duration: '30s', target: 5 },
                { duration: '30', target: 5 },
                { duration: '30s', target: 0 },
            ],
            exec: 'heavy_request',
        },
    },
};

export function lightweight_request() {
    const res = http.get('http://host.docker.internal:8080/benchmark?count=1&durationMs=1&parallelism=1');
    check(res, { 'status is 200': (r) => r.status === 200 });
    sleep(1);
}

export function moderate_request() {
    const res = http.get('http://host.docker.internal:8080/benchmark?count=20&durationMs=5&parallelism=1');
    check(res, { 'status is 200': (r) => r.status === 200 });
    sleep(1);
}

export function heavy_request() {
    const res = http.get('http://host.docker.internal:8080/benchmark?count=40&durationMs=10&parallelism=2');
    check(res, { 'status is 200': (r) => r.status === 200 });
    sleep(1);
}
