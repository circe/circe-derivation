namespace java io.circe.examples.scrooge

struct something_struct {
  1: required string a;
  2: optional i64 b;
  3: list<string> c;
}

struct bigger_struct {
  1: something_struct d
  2: optional string e;
}
