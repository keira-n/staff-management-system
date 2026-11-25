export interface Staff {
    id?: string;
    name: string;
    email: string;
    phone?: string;
    employeeCode?: string;
    department?: string;
    position?: string;
    organisationName?: string;
    organisationType?: string;
    organisationId?: string;
    customFields?: Record<string, unknown>;
}
