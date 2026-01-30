import java.time.LocalDateTime;

void main() {    
    println(this.getClass().getName());
    while (true) {
        try {
            printInfo();
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println("Had exception" + e.getMessage());
            e.printStackTrace();
        }
    }
}

void printInfo() {
    println("Generated %d at %s".formatted(randomNumber(), getDateTime()));
}

int randomNumber() {
    return (int) (Math.random() * 1000);
}

String getDateTime() {
    return LocalDateTime.now().toString();
}