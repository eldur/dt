package info.dt.data;

public enum Status {
  NONE(0), XXX(1), TODO(2), FIXME(3);

  private Integer prio;

  Status(int prio) {
    this.prio = Integer.valueOf(prio);

  }

  public boolean greaterThen(Status status) {

    return prio.compareTo(status.prio) > 0;
  }
}
