import random


def write_random(target_size_in_gb: float, filename: str = "test.txt") -> None:
    target_size = int(target_size_in_gb * 1024 * 1024 * 1024)
    size_written = 0
    buffer_size = 10_000

    with(open(filename, "w")) as file:
        while size_written < target_size:
            batch = "\n".join(str(random.randint(1, 1_000_000)) for _ in range(buffer_size))
            batch += "\n"
            file.write(batch)
            size_written += len(batch)


if __name__ == "__main__":
    write_random(0.1, "input.txt")
