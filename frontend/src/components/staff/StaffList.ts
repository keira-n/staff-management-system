import type { Staff } from '../../types/Staff';
import { renderStaffCard } from './StaffCard';

interface StaffListOptions {
    onEdit: (staff: Staff) => void;
    onDelete: (staff: Staff) => void;
}

export function renderStaffList(
    container: HTMLElement,
    staffList: Staff[],
    options: StaffListOptions
): void {
    let searchQuery = '';

    function render(): void {
        container.innerHTML = '';

        const section = document.createElement('section');
        section.className = 'staff-list-section';

        const heading = document.createElement('div');
        heading.className = 'staff-list-heading';

        const title = document.createElement('h2');
        title.textContent = `Staff Members (${staffList.length})`;

        const searchInput = document.createElement('input');
        searchInput.type = 'search';
        searchInput.placeholder = 'Search staff...';
        searchInput.className = 'staff-search';
        searchInput.value = searchQuery;

        heading.append(title, searchInput);

        const cardsContainer = document.createElement('div');
        cardsContainer.className = 'staff-grid';

        const filteredStaff = staffList.filter(staff =>
            JSON.stringify(staff)
                .toLowerCase()
                .includes(searchQuery.toLowerCase())
        );

        if (filteredStaff.length === 0) {
            const emptyMessage = document.createElement('p');
            emptyMessage.className = 'empty-message';

            emptyMessage.textContent = searchQuery
                ? 'No staff members match your search.'
                : 'No staff members have been added yet.';

            cardsContainer.appendChild(emptyMessage);
        } else {
            filteredStaff.forEach(staff => {
                const card = renderStaffCard(staff, {
                    onEdit: () => options.onEdit(staff),
                    onDelete: () => options.onDelete(staff)
                });

                cardsContainer.appendChild(card);
            });
        }

        searchInput.addEventListener('input', () => {
            searchQuery = searchInput.value;
            render();

            const updatedInput =
                container.querySelector<HTMLInputElement>('.staff-search');

            updatedInput?.focus();
            updatedInput?.setSelectionRange(
                searchQuery.length,
                searchQuery.length
            );
        });

        section.append(heading, cardsContainer);
        container.appendChild(section);
    }

    render();
}
