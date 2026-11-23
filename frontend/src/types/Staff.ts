export interface Staff {
    id?: string;
    name: string;
    email: string;
    phone?: string;
    organisationName?: string;
    organisationType?: string;
    organisationId?: string;
    employeeCode?: string;
    department?: string;
    position?: string;
    customFields?: Record<string, unknown>;
}
