const API_URL = "http://localhost:8080/api/staff";

export async function getStaff() {
    const response = await fetch(API_URL);

    if (!response.ok) {
        throw new Error("Failed to fetch staff");
    }

    return response.json();
}

export async function addStaff(staff: any) {
    const response = await fetch(API_URL, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(staff)
    });

    if (!response.ok) {
        throw new Error("Failed to add staff");
    }

    return response.json();
}

export async function updateStaff(id: string, staff: any) {
    const response = await fetch(`${API_URL}/${id}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(staff)
    });

    if (!response.ok) {
        throw new Error("Failed to update staff");
    }

    return response.json();
}

export async function deleteStaff(id: string) {
    const response = await fetch(`${API_URL}/${id}`, {
        method: "DELETE"
    });

    if (!response.ok) {
        throw new Error("Failed to delete staff");
    }
}