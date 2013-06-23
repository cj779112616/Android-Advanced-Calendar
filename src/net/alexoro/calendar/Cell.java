package net.alexoro.calendar;

/**
 * User: UAS
 * Date: 20.06.13
 * Time: 22:55
 */
class Cell {

    public int row;
    public int column;

    public Cell(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public void update(int row, int column) {
        this.row = row;
        this.column = column;
    }

}