export interface Category {
    id: number | string;
    dataType: string;
    text: string;
    label: number | null;
    iconName: string | null;
    iconUnicode: string | null;
    parent: Category | null;
    checked?: boolean;
}
