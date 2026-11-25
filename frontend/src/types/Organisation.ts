export interface Organisation {
    id?: string;
    name: string;
    type: string;
    description?: string;
    databaseName?: string;
    databaseStatus?: 'PROVISIONING' | 'ACTIVE' | 'FAILED';
}
