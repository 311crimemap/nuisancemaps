export interface Category {
    id: number;
    dataType: string;
    text: string;
    label: number;
    iconName: string;
    iconUnicode: string;
    parent: Category | null;
}
