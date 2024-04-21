import { Provider } from '@angular/core';

export function autoMock<T>(obj: new (...args: any[]) => T): T {
  const res = {} as any;

  const keys = Object.getOwnPropertyNames(obj.prototype);

  const allMethods = keys.filter((key) => {
    try {
      return typeof obj.prototype[key] === 'function';
    } catch (error) {
      return false;
    }
  });

  const allProperties = keys.filter((x) => !allMethods.includes(x));

  allMethods.forEach((method) => (res[method] = jest.fn()));

  allProperties.forEach((property) => {
    Object.defineProperty(res, property, {
      get: function () {
        return '';
      },
      configurable: true,
    });
  });

  return res;
}

export function provideAbstractMock<T, M extends T>(
  type: abstract new (...args: any[]) => T,
  mockType: new (...args: any[]) => M
): Provider {
  const mock = autoMock(mockType);

  return { provide: type, useValue: mock };
}

export function provideMock<T>(type: new (...args: any[]) => T): Provider {
  const mock = autoMock(type);

  return { provide: type, useValue: mock };
}
